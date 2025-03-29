package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import forms.AssignmentForm;
import models.dto.AssignmentUploadContext;
import models.enums.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Files;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import services.AuthenticationService;
import services.AuthorizationService;
import services.core.AssignmentService;

import javax.inject.Singleton;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Security.Authenticated(AuthenticationService.class)
@Singleton
public class FormsController extends Controller {
    private final AssignmentService assignmentService;
    private static final Set<Roles> ALLOWED_ROLES = Set.of(Roles.PROFESSOR);
    private final AuthorizationService authorizationService;
    private static final Logger log = LoggerFactory.getLogger(FormsController.class);

    @Inject
    public FormsController(AssignmentService assignmentService, AuthorizationService authorizationService) {
        this.assignmentService = assignmentService;
        this.authorizationService = authorizationService;
    }

    public CompletionStage<Result> createAssignment(Http.Request request) {

        if(!authorizationService.isAuthorized(request, ALLOWED_ROLES)){
            return CompletableFuture.failedFuture(new IllegalAccessException("Unauthorized access"));
        }

        Http.MultipartFormData<Files.TemporaryFile> body = request.body().asMultipartFormData();

        return extractAndValidateAssignmentForm(request).thenCompose(assignment -> {
            if (assignment == null) {
                log.error("Assignment form is null");
                return CompletableFuture.completedFuture(badRequest("Invalid assignment form"));
            }

            // Set up context with courseCode and body
            AssignmentUploadContext assignmentUploadContext = new AssignmentUploadContext();
            assignmentUploadContext.setCourseCode(assignment.getCourseCode());
            assignmentUploadContext.setBody(body);

            return assignmentService.parseAssignmentTaskTeamInfo(assignmentUploadContext)
                    .thenCompose(reviewTasks -> {
                        if (reviewTasks == null) {
                            log.error("Review tasks are null");
                            return CompletableFuture.completedFuture(badRequest("Invalid review tasks"));
                        }
                        return assignmentService.createAssignment(assignment, reviewTasks);
                    });
        }).exceptionally(ex -> {
            log.error("Error in createAssignment: {}", ex.getMessage());
            return unauthorized("Error: " + ex.getMessage());
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
                    log.error("Error in updateAssignment: {}", ex.getMessage());
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
}
