package controllers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static play.mvc.Http.Status.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.JsonNode;
import models.dto.AssignmentExportDTO;
import models.dto.FeedbackDTO;
import models.enums.Roles;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import services.AuthorizationService;
import services.export.ExportService;

public class FileExportControllerTest {

    @InjectMocks private FileExportController controller;

    @Mock private ExportService exportService;

    @Mock private AuthorizationService authorizationService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new FileExportController(exportService, authorizationService);
    }

    @Test
    public void testDownloadExcelReport_unauthorized_returns401() throws Exception {
        Http.Request request = mock(Http.Request.class);
        when(authorizationService.isAuthorized(request, Set.of(Roles.PROFESSOR))).thenReturn(false);

        Result result = controller.downloadExcelReport("CS101", 1L, request).toCompletableFuture().get();

        assertEquals(UNAUTHORIZED, result.status());
        assertEquals("Unauthorized access", Helpers.contentAsString(result));
    }

    @Test
    public void testDownloadExcelReport_success_returnsExcelFile() throws Exception {
        Http.Request request = mock(Http.Request.class);
        when(authorizationService.isAuthorized(request, Set.of(Roles.PROFESSOR))).thenReturn(true);

        List<AssignmentExportDTO> mockExportData = List.of();
        byte[] fakeBytes = "excel-content".getBytes();

        when(exportService.getAssignmentExportData(1L))
                .thenReturn(CompletableFuture.completedFuture(mockExportData));

        when(exportService.exportToExcel(mockExportData))
                .thenReturn(CompletableFuture.completedFuture(fakeBytes));

        Result result = controller.downloadExcelReport("CS101", 1L, request).toCompletableFuture().get();

        assertEquals(OK, result.status());
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", result.contentType().orElse(""));
        assertTrue(result.header("Content-Disposition").orElse("").contains("review_report.xlsx"));
    }

    @Test
    public void testDownloadStudentFeedbackReport_unauthorized_returns401() throws Exception {
        Http.Request request = Helpers.fakeRequest().build();
        when(authorizationService.isAuthorized(request, Set.of(Roles.PROFESSOR))).thenReturn(false);

        Result result = controller.downloadStudentFeedbackReport(request).toCompletableFuture().get();

        assertEquals(UNAUTHORIZED, result.status());
        assertEquals("Unauthorized access", Helpers.contentAsString(result));
    }

    @Test
    public void testDownloadStudentFeedbackReport_invalidJson_returns400() throws Exception {
        JsonNode invalidJson = Json.newObject();

        Http.Request request = Helpers.fakeRequest()
                .bodyJson(invalidJson)
                .build();

        when(authorizationService.isAuthorized(any(), eq(Set.of(Roles.PROFESSOR)))).thenReturn(true);

        Result result = controller.downloadStudentFeedbackReport(request).toCompletableFuture().get();

        assertEquals(BAD_REQUEST, result.status());
        assertEquals("Invalid request body", Helpers.contentAsString(result));
    }

    @Test
    public void testDownloadStudentFeedbackReport_success_returnsExcelFile() throws Exception {

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("studentName", "John Student");
        jsonMap.put("userId", 1L);
        jsonMap.put("email", "john@example.com");
        jsonMap.put("status", "Completed");
        jsonMap.put("averageFeedbackScore", 4.5);
        jsonMap.put("maximumAverageFeedbackScoreForReviewTask", 5.0);
        jsonMap.put("overallClassAverage", 3.8);
        jsonMap.put("feedbacks", Map.of(1L, List.of(new FeedbackDTO())));
        jsonMap.put("evaluationMatrix", List.of());
        jsonMap.put("classAverages", Map.of("Q1", 3.5f));
        jsonMap.put("reviewerAverages", List.of(4.2f, 4.8f));

        JsonNode jsonBody = Json.toJson(jsonMap);

        Http.Request request = Helpers.fakeRequest()
                .bodyJson(jsonBody)
                .build();

        when(authorizationService.isAuthorized(request, Set.of(Roles.PROFESSOR))).thenReturn(true);

        byte[] fileBytes = "student-feedback-content".getBytes();
        when(exportService.exportFeedbackForStudent(
                anyMap(),
                eq("John Student"),
                eq(1L),
                eq("john@example.com"),
                eq("Completed"),
                eq(4.5f),
                eq(5.0f),
                anyList(),
                anyMap(),
                eq(3.8f),
                anyList()))
                .thenReturn(CompletableFuture.completedFuture(fileBytes));

        Result result = controller.downloadStudentFeedbackReport(request).toCompletableFuture().get();

        assertEquals(OK, result.status());
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", result.contentType().orElse(""));
        assertTrue(result.header("Content-Disposition").orElse("").contains("John_Student_feedback.xlsx"));
    }
}