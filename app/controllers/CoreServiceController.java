package controllers;

import static play.mvc.Results.*;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.enums.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import services.AuthenticationService;
import services.AuthorizationService;
import services.core.AssignmentService;
import services.core.CourseService;
import services.core.ReviewTaskService;

@Security.Authenticated(AuthenticationService.class)
@Singleton
public class CoreServiceController {

    private static final Set<Roles> ALLOWED_ROLES = Set.of(Roles.PROFESSOR);
    private static final Logger log = LoggerFactory.getLogger(FormsController.class);
    private final AuthorizationService authorizationService;
    private final CourseService courseService;
    private final AssignmentService assignmentService;
    private final ReviewTaskService reviewTaskService;

    @Inject
    public CoreServiceController(AuthorizationService authorizationService, CourseService courseService, AssignmentService assignmentService, ReviewTaskService reviewTaskService) {
        this.authorizationService = authorizationService;
        this.courseService = courseService;
        this.assignmentService = assignmentService;
        this.reviewTaskService = reviewTaskService;
    }


    public CompletionStage<Result> getAllCourses(Http.Request request) {

        if(!authorizationService.isAuthorized(request, ALLOWED_ROLES)){
            return CompletableFuture.completedFuture(unauthorized("Unauthorized access"));
        }

        Long userId = Long.valueOf(request.session().get("userId").get());
        return courseService.getAllCourses(userId).thenApply(courseList -> ok(Json.toJson(courseList)));
    }

    public CompletionStage<Result> getAssignmentDetails(Long assignmentId, Http.Request request) {
            if(!authorizationService.isAuthorized(request, ALLOWED_ROLES)){
                return CompletableFuture.completedFuture(unauthorized("Unauthorized access"));
            }

            return assignmentService.getAssignmentDetails(assignmentId)
                    .thenApply(assignment -> ok(Json.toJson(assignment)))
                    .exceptionally(ex -> {
                        log.error("Error in getAssignmentDetails: {}", ex.getMessage());
                        return badRequest(ex.getMessage());
                    });
    }

    public CompletionStage<Result> getReviewTasksSubmissionOverview(Long assignmentId, Http.Request request){
        if(!authorizationService.isAuthorized(request, ALLOWED_ROLES)){
            return CompletableFuture.completedFuture(unauthorized("Unauthorized access"));
        }

        return reviewTaskService.getReviewTasksSubmissionOverview(assignmentId)
                .thenApply(
                        submissionOverview -> {
                            log.info("Successfully fetched submission overview for assignment {}", assignmentId);
                            return ok(Json.toJson(submissionOverview));
                        })
                .exceptionally(ex -> {
                    log.error("Error in getReviewTasksSubmissionOverview: {}", ex.getMessage());
                    return badRequest(ex.getMessage());
                });
    }

    public CompletionStage<Result> fetchAssignmentsForCourse(String courseCode, Http.Request request) {
        if(!authorizationService.isAuthorized(request, ALLOWED_ROLES)){
            return CompletableFuture.completedFuture(unauthorized("Unauthorized access"));
        }

        return assignmentService.fetchAssignmentsForCourse(courseCode)
                .thenApply(assignments -> ok(Json.toJson(assignments)))
                .exceptionally(ex -> {
                    log.error("Error in fetchAssignmentsForCourse: {}", ex.getMessage());
                    return badRequest(ex.getMessage());
                });
    }


    public CompletionStage<Result> deleteAssignment(Long assignmentId, Http.Request request) {
        if(!authorizationService.isAuthorized(request, ALLOWED_ROLES)){
            return CompletableFuture.completedFuture(unauthorized("Unauthorized access"));
        }


        JsonNode json = request.body().asJson();
        String courseCode = json != null && json.has("courseCode") ? json.get("courseCode").asText() : "Course code not provided";

        return assignmentService.deleteAssignment(assignmentId)
                .thenApply(deleted -> {
                    ObjectNode responseJson = Json.newObject();
                    responseJson.put("assignmentId", assignmentId);
                    responseJson.put("courseCode", courseCode);

                    if (deleted) {
                        responseJson.put("message", "Assignment deleted successfully");
                        return ok(responseJson);
                    } else {
                        responseJson.put("message", "Assignment not found");
                        return notFound(responseJson);
                    }
                })
                .exceptionally(ex -> {
                    log.error("Error in deleteAssignment: {}", ex.getMessage());
                    ObjectNode errorJson = Json.newObject();
                    errorJson.put("error", "Failed to delete assignment");
                    errorJson.put("details", ex.getMessage());
                    return badRequest(errorJson);
                });
    }
}
