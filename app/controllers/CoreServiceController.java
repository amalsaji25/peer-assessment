package controllers;

import static play.mvc.Results.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.name.Named;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.enums.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import services.AuthenticationService;
import services.AuthorizationService;
import services.core.*;
import services.report.ReportService;

/**
 * CoreServiceController handles core functionalities such as course management, assignment details,
 * review tasks, and report generation. It uses Play Framework's dependency injection and asynchronous
 * programming model.
 */
@Security.Authenticated(AuthenticationService.class)
@Singleton
public class CoreServiceController {

    private static final Set<Roles> ALLOWED_ROLES = Set.of(Roles.PROFESSOR);
    private static final Logger log = LoggerFactory.getLogger(FormsController.class);
    private final AuthorizationService authorizationService;
    private final CourseService courseService;
    private final AssignmentService assignmentService;
    private final ReviewTaskService reviewTaskService;
    private final EnrollmentService enrollmentService;
    private final UserService userService;
    private final ReportService professorReportService;
    private final ReportService studentReportService;

    @Inject
    public CoreServiceController(AuthorizationService authorizationService, CourseService courseService, AssignmentService assignmentService, ReviewTaskService reviewTaskService, EnrollmentService enrollmentService, UserService userService, @Named("professor")ReportService professorReportService, @Named("student") ReportService studentReportService) {
        this.authorizationService = authorizationService;
        this.courseService = courseService;
        this.assignmentService = assignmentService;
        this.reviewTaskService = reviewTaskService;
        this.enrollmentService = enrollmentService;
        this.userService = userService;
        this.professorReportService = professorReportService;
        this.studentReportService = studentReportService;
    }

    /**
     * UnAssigns a course from a professor.
     * @param courseCode the course code to be assigned
     * @param request the incoming HTTP request object
     * @return a Result indicating the success or failure of the operation
     */
    public CompletionStage<Result> unassignCourse(String courseCode, Http.Request request) {
        log.info("Unassigning course");
        if(!authorizationService.isAuthorized(request, Set.of(Roles.PROFESSOR))){
            log.info("Not authorized");
            return CompletableFuture.failedFuture(new IllegalAccessException("Unauthorized access"));
        }
        if (courseCode == null || courseCode.isEmpty()) {
            log.error("Course code is null or empty");
            return CompletableFuture.completedFuture(badRequest("Invalid course code"));
        }

        return courseService.unassignCourse(courseCode)
                .thenApply(unassigned -> {
                    if (unassigned) {
                        log.info("Course {} unassigned successfully", courseCode);
                        return ok(Json.newObject().put("message", "Course unassigned successfully"));
                    } else {
                        log.error("Failed to unassign course {}", courseCode);
                        return notFound(Json.newObject().put("message", "Course not found"));
                    }
                })
                .exceptionally(ex -> {
                    log.error("Error in unassigning course: {}", ex.getMessage());
                    return internalServerError(Json.newObject().put("message", "Internal server error"));
                });
    }

    /**
     * Fetch all the courses taught by a professor for a given term.
     * @param term the term for which to fetch courses
     * @param request the incoming HTTP request object
     * @return a Result containing the list of courses in JSON format
     */
    public CompletionStage<Result> getAllCourses(String term, Http.Request request) {

        if(!authorizationService.isAuthorized(request, ALLOWED_ROLES)){
            return CompletableFuture.completedFuture(unauthorized("Unauthorized access"));
        }

        Long userId = Long.valueOf(request.session().get("userId").get());
        return courseService.getAllCourses(userId, term).thenApply(courseList -> ok(Json.toJson(courseList)));
    }

    /**
     * Fetch all the terms available in the system for a given professor.
     * @param request the incoming HTTP request object
     * @return a Result containing the list of terms in JSON format
     */
    public CompletionStage<Result> getAllTerms(Http.Request request) {
        if(!authorizationService.isAuthorized(request, ALLOWED_ROLES)){
            return CompletableFuture.completedFuture(unauthorized("Unauthorized access"));
        }

        Long userId = Long.valueOf(request.session().get("userId").get());
        return courseService.getAllTerms(userId).thenApply(termList -> ok(Json.toJson(termList)));
    }

    /**
     * Fetch all the courses a student is enrolled in.
     * @param request the incoming HTTP request object
     * @return a Result containing the list of courses in JSON format
     */
    public CompletableFuture<Result> getStudentEnrolledCourses(Http.Request request){
        if(!authorizationService.isAuthorized(request, Collections.singleton(Roles.STUDENT))){
            return CompletableFuture.completedFuture(unauthorized("Unauthorized access"));
        }

        Long userId = Long.valueOf(request.session().get("userId").get());
        return enrollmentService.getStudentEnrolledCourse(userId).thenApply(courseList -> ok(Json.toJson(courseList)));
    }

    /**
     * Fetch the assignment details for a given assignment ID.
     * @param assignmentId the ID of the assignment to fetch
     * @param request the incoming HTTP request object
     * @return a Result containing the assignment details in JSON format
     */
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


    /**
     * Fetch the review tasks submission overview for a given assignment ID.
     * @param assignmentId the ID of the assignment to fetch
     * @param request the incoming HTTP request object
     * @return a Result containing the review tasks submission overview in JSON format
     */
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

    /**
     * Fetch all the assignments for a given course code.
     * @param courseCode the course code to fetch assignments for
     * @param request the incoming HTTP request object
     * @return a Result containing the list of assignments in JSON format
     */
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

    /**
     * Delete an assignment for a given assignment ID.
     * @param assignmentId the ID of the assignment to delete
     * @param request the incoming HTTP request object
     * @return a Result indicating the success or failure of the operation
     */
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

    /**
     * Validate a professor by checking if the professor ID exists.
     * @param professorId the ID of the professor to validate
     * @param request the incoming HTTP request object
     * @return a Result indicating whether the professor is valid or not
     */
    public CompletionStage<Result> validateProfessor(Long professorId, Http.Request request) {
        if(!authorizationService.isAuthorized(request, Set.of(Roles.ADMIN))){
            return CompletableFuture.completedFuture(unauthorized("Unauthorized access"));
        }

        return userService.validateProfessor(professorId)
                .thenApply(isValid -> {
                    log.info("Validation result for professor {}: {}", professorId, isValid);
                    ObjectNode responseJson = Json.newObject();
                    responseJson.put("professorId", professorId);
                    responseJson.put("isValid", isValid);
                    return ok(responseJson);
                })
                .exceptionally(ex -> {
                    log.error("Error in validateProfessor: {}", ex.getMessage());
                    ObjectNode errorJson = Json.newObject();
                    errorJson.put("error", "Failed to validate professor");
                    errorJson.put("details", ex.getMessage());
                    return badRequest(errorJson);
                });
    }

    /**
     * Fetch the detailed summary of an assignment for a given assignment ID.
     * @param assignmentId the ID of the assignment to fetch
     * @param request the incoming HTTP request object
     * @return a new HTML page containing the detailed summary of the assignment
     */
    public CompletionStage<Result> getAssignmentDetailedSummary(Long assignmentId, Http.Request request) {
        if(!authorizationService.isAuthorized(request, ALLOWED_ROLES)){
            return CompletableFuture.completedFuture(unauthorized("Unauthorized access"));
        }
        Long userId = Long.valueOf(request.session().get("userId").get());
        return professorReportService.generateReport(assignmentId, userId)
                .thenApply(reportDTO -> ok(views.html.detailedAssignmentReport.render(reportDTO)));
    }

    /**
     * Fetch the summary of an assignment for a given assignment ID.
     * @param assignmentId the ID of the assignment to fetch
     * @param request the incoming HTTP request object
     * @return a new HTML page containing the summary of the assignment
     */
    public CompletionStage<Result> getAssignmentSummary(Long assignmentId, Http.Request request) {
        if(!authorizationService.isAuthorized(request, ALLOWED_ROLES)){
            return CompletableFuture.completedFuture(unauthorized("Unauthorized access"));
        }
        Long userId = Long.valueOf(request.session().get("userId").get());
        return professorReportService.generateReport(assignmentId, userId)
                .thenApply(reportDTO -> ok(views.html.assignmentSummaryReport.render(reportDTO)));
    }

    /**
     * Fetch the detailed report of a student's assignment for a given assignment ID.
     * @param assignmentId the ID of the assignment to fetch
     * @param request the incoming HTTP request object
     * @return a new HTML page containing the detailed report of the student's assignment
     */
    public CompletionStage<Result> getStudentAssignmentReport(Long assignmentId, Http.Request request) {
        if(!authorizationService.isAuthorized(request, Set.of(Roles.STUDENT))){
            return CompletableFuture.completedFuture(unauthorized("Unauthorized access"));
        }
        Long userId = Long.valueOf(request.session().get("userId").get());
        return studentReportService.generateReport(assignmentId, userId)
                .thenApply(reportDTO -> ok(views.html.studentDetailedReport.render(reportDTO)));
    }

}
