package controllers;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.*;

import com.fasterxml.jackson.databind.JsonNode;
import models.enums.Roles;
import org.junit.Before;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import services.*;
import services.core.*;
import services.report.ReportService;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class CoreServiceControllerTest {

    private AuthorizationService authService;
    private CourseService courseService;
    private AssignmentService assignmentService;
    private ReviewTaskService reviewTaskService;
    private EnrollmentService enrollmentService;
    private UserService userService;
    private ReportService professorReportService;
    private ReportService studentReportService;

    private CoreServiceController controller;

    @Before
    public void setUp() {
        authService = mock(AuthorizationService.class);
        courseService = mock(CourseService.class);
        assignmentService = mock(AssignmentService.class);
        reviewTaskService = mock(ReviewTaskService.class);
        enrollmentService = mock(EnrollmentService.class);
        userService = mock(UserService.class);
        professorReportService = mock(ReportService.class);
        studentReportService = mock(ReportService.class);

        controller = new CoreServiceController(
                authService,
                courseService,
                assignmentService,
                reviewTaskService,
                enrollmentService,
                userService,
                professorReportService,
                studentReportService
        );
    }

    @Test
    public void testUnassignCourse_success() throws Exception {
        String courseCode = "CS101";
        Http.Request request = mock(Http.Request.class);
        when(authService.isAuthorized(eq(request), anySet())).thenReturn(true);
        when(courseService.unassignCourse(courseCode)).thenReturn(CompletableFuture.completedFuture(true));

        CompletionStage<Result> resultStage = controller.unassignCourse(courseCode, request);
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(OK, result.status());
        JsonNode json = Json.parse(contentAsString(result));
        assertEquals("Course unassigned successfully", json.get("message").asText());
    }

    @Test
    public void testGetAllCourses_unauthorized() throws Exception {
        Http.Request request = mock(Http.Request.class);
        when(authService.isAuthorized(eq(request), anySet())).thenReturn(false);

        CompletionStage<Result> resultStage = controller.getAllCourses("Fall2025", request);
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(UNAUTHORIZED, result.status());
    }

    @Test
    public void testValidateProfessor_success() throws Exception {
        Long profId = 456L;
        Http.Request request = mock(Http.Request.class);
        when(authService.isAuthorized(eq(request), eq(Set.of(Roles.ADMIN)))).thenReturn(true);
        when(userService.validateProfessor(profId)).thenReturn(CompletableFuture.completedFuture(true));

        CompletionStage<Result> resultStage = controller.validateProfessor(profId, request);
        Result result = resultStage.toCompletableFuture().get();

        assertEquals(OK, result.status());
        JsonNode json = Json.parse(contentAsString(result));
        assertTrue(json.get("isValid").asBoolean());
        assertEquals(profId.longValue(), json.get("professorId").asLong());
    }
}