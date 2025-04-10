package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import forms.AssignmentForm;
import models.User;
import models.dto.Context;
import models.enums.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Files;
import play.libs.Json;
import play.mvc.*;
import services.AuthenticationService;
import services.AuthorizationService;
import services.core.AssignmentService;
import services.core.ReviewTaskService;
import services.processors.Processor;
import services.processors.ProcessorStrategy;
import services.processors.record.FormInputRecord;
import services.processors.record.InputRecord;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Security.Authenticated(AuthenticationService.class)
@Singleton
public class FormsController extends Controller {
    private final AssignmentService assignmentService;
    private final ReviewTaskService reviewTaskService;
    private final ProcessorStrategy processorStrategy;
    private static final Set<Roles> ALLOWED_ROLES = Set.of(Roles.PROFESSOR);
    private final AuthorizationService authorizationService;
    private static final Logger log = LoggerFactory.getLogger(FormsController.class);

    @Inject
    public FormsController(AssignmentService assignmentService, AuthorizationService authorizationService, ReviewTaskService reviewTaskService,ProcessorStrategy processorStrategy) {
        this.assignmentService = assignmentService;
        this.authorizationService = authorizationService;
        this.reviewTaskService = reviewTaskService;
        this.processorStrategy = processorStrategy;
    }

    public CompletionStage<Result> createAssignment(Http.Request request) {

        if(!authorizationService.isAuthorized(request, ALLOWED_ROLES)){
            return CompletableFuture.failedFuture(new IllegalAccessException("Unauthorized access"));
        }

        return extractAndValidateAssignmentForm(request).thenCompose(assignment -> {
            if (assignment == null) {
                log.error("Assignment form is null");
                ObjectNode errorJson = Json.newObject();
                errorJson.put("error", "Invalid assignment form");
                return CompletableFuture.completedFuture(badRequest(errorJson));
            }
            return assignmentService.createAssignment(assignment)
                    .thenApply(done -> {
                        ObjectNode successJson = Json.newObject();
                        successJson.put("message", "Assignment created successfully");
                        return ok(successJson);
                    });
        }).exceptionally(ex -> {
            log.error("Error in creating Assignment: {}", ex.getMessage());
            ObjectNode errorJson = Json.newObject();
            errorJson.put("error", "Error creating assignment: " + ex.getMessage());
            return badRequest(errorJson);
        });
    }

    public CompletionStage<Result> updateAssignment(Long assignmentId, Http.Request request) {

        if(!authorizationService.isAuthorized(request, ALLOWED_ROLES)){
            return CompletableFuture.failedFuture(new IllegalAccessException("Unauthorized access"));
        }

        return extractAndValidateAssignmentForm(request)
                .thenCompose(form -> {
                    if (form == null) {
                        ObjectNode errorJson = Json.newObject();
                        errorJson.put("error", "Invalid assignment form");
                        return CompletableFuture.completedFuture(badRequest(errorJson));
                    }
                    return assignmentService.updateAssignment(assignmentId, form)
                            .thenApply(done -> {
                                ObjectNode successJson = Json.newObject();
                                successJson.put("message", "Assignment updated successfully");
                                successJson.put("assignmentId", assignmentId);
                                return ok(successJson);
                            });
                })
                .exceptionally(ex -> {
                    log.error("Error in updateAssignment: {}", ex.getMessage(),ex);
                    ObjectNode errorJson = Json.newObject();
                    errorJson.put("error", "Error updating assignment: " + ex.getMessage());
                    return badRequest(errorJson);
                });
    }

    private CompletableFuture<AssignmentForm> extractAndValidateAssignmentForm(Http.Request request) {
        if(!authorizationService.isAuthorized(request, ALLOWED_ROLES)){
            return CompletableFuture.failedFuture(new IllegalAccessException("Unauthorized access"));
        }

        log.info("Extracting assignment form");
        String sessionId = request.session().get("userId").orElse("NONE");
        log.info("Session ID: {}", sessionId);

        if (!authorizationService.isAuthorized(request, ALLOWED_ROLES)) {
            log.info("Not authorized");
            return CompletableFuture.failedFuture(new IllegalAccessException("Unauthorized access"));
        }

        Http.MultipartFormData<Files.TemporaryFile> body = request.body().asMultipartFormData();
        if (body == null) {
            log.error("Expected a multipart form data");
            return CompletableFuture.failedFuture(new IllegalArgumentException("Missing MultipartFormData"));
        }

        return assignmentService.parseAssignmentForm(body);
    }

    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> saveOrSubmitReviewTask(Long reviewTaskId, Http.Request request) {
        JsonNode json = request.body().asJson();

        return reviewTaskService.parseAndSaveOrSubmitReviewTask(reviewTaskId, json)
                .thenApply(Results::ok)
                .exceptionally(ex -> Results.badRequest("Invalid data: " + ex.getMessage()));
    }


    public CompletionStage<Result> createUser(Http.Request request) {
        log.info("Creating user");
        if(!authorizationService.isAuthorized(request, Set.of(Roles.ADMIN))){
            log.info("Not authorized");
            return CompletableFuture.failedFuture(new IllegalAccessException("Unauthorized access"));
        }

        Http.MultipartFormData<Files.TemporaryFile> body = request.body().asMultipartFormData();
        if (body == null) {
            log.error("Multipart body is null");
            return CompletableFuture.completedFuture(badRequest("Invalid multipart form data"));
        }

        Map<String, String[]> formData = body.asFormUrlEncoded();
        if (formData == null || formData.isEmpty()) {
            log.error("Form data inside multipart is null or empty");
            return CompletableFuture.completedFuture(badRequest("Invalid form submission"));
        }

        InputRecord  formRecord = new FormInputRecord(formData);
        Context context = new Context();

        Processor<User, InputRecord> processor = processorStrategy.getFormProcessor("userForm");
        log.info("Processor for user form: {}", processor);

        return processor.processData(formRecord, context)
                .thenCompose(processedData -> processor.saveProcessedData(processedData, context))
                .thenApply(msg -> ok(Json.toJson(Collections.singletonMap("message", "User Created!"))))
                .exceptionally(ex -> {
                    log.error("File processing failed with error: {}", ex.getMessage());
                    Throwable cause = ex;
                    while (cause.getCause() != null) {
                        cause = cause.getCause();
                    }
                    String userMessage = cause.getMessage() != null ? cause.getMessage() : "An unexpected error occurred.";

                    ObjectNode errorJson = Json.newObject();
                    errorJson.put("error", userMessage);
                    return badRequest(Json.toJson(Collections.singletonMap("error", userMessage)));
                });
    }


    public CompletionStage<Result> createCourse(Http.Request request) {
        log.info("Creating course");
        if(!authorizationService.isAuthorized(request, Set.of(Roles.ADMIN))){
            log.info("Not authorized");
            return CompletableFuture.failedFuture(new IllegalAccessException("Unauthorized access"));
        }

        Http.MultipartFormData<Files.TemporaryFile> body = request.body().asMultipartFormData();
        if (body == null) {
            log.error("Multipart body is null");
            return CompletableFuture.completedFuture(badRequest("Invalid multipart form data"));
        }

        Map<String, String[]> formData = body.asFormUrlEncoded();
        if (formData == null || formData.isEmpty()) {
            log.error("Form data inside multipart is null or empty");
            return CompletableFuture.completedFuture(badRequest("Invalid form submission"));
        }

        InputRecord  formRecord = new FormInputRecord(formData);
        Context context = new Context();

        Processor<User, InputRecord> processor = processorStrategy.getFormProcessor("courseForm");
        log.info("Processor for course form: {}", processor);

        return processor.processData(formRecord, context)
                .thenCompose(processedData -> processor.saveProcessedData(processedData, context))
                .thenApply(msg -> ok(Json.toJson(Collections.singletonMap("message", "Course Created!"))))
                .exceptionally(ex -> {
                    log.error("File processing failed with error: {}", ex.getMessage());
                    Throwable cause = ex;
                    while (cause.getCause() != null) {
                        cause = cause.getCause();
                    }
                    String userMessage = cause.getMessage() != null ? cause.getMessage() : "An unexpected error occurred.";

                    ObjectNode errorJson = Json.newObject();
                    errorJson.put("error", userMessage);
                    return badRequest(Json.toJson(Collections.singletonMap("error", userMessage)));
                });
        }

}
