package controllers;

import static play.mvc.Results.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.dto.FeedbackDTO;
import models.enums.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import services.AuthenticationService;
import services.AuthorizationService;
import services.export.ExportService;

@Security.Authenticated(AuthenticationService.class)
@Singleton
public class FileExportController {

  private static final Logger log = LoggerFactory.getLogger(FileExportController.class);
  private static final Set<Roles> ALLOWED_ROLES = Set.of(Roles.PROFESSOR);
  private final ExportService exportService;
  private final AuthorizationService authorizationService;

  @Inject
  public FileExportController(
      ExportService exportService, AuthorizationService authorizationService) {
    this.exportService = exportService;
    this.authorizationService = authorizationService;
  }

  public CompletionStage<Result> downloadExcelReport(
      String courseCode, Long assignmentId, Http.Request request) {
    if (!authorizationService.isAuthorized(request, ALLOWED_ROLES)) {
      return CompletableFuture.completedFuture(unauthorized("Unauthorized access"));
    }

    return exportService
        .getAssignmentExportData(courseCode, assignmentId)
        .thenCompose(exportService::exportToExcel)
        .thenApply(
            excelBytes ->
                ok(excelBytes)
                    .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .withHeader("Content-Disposition", "attachment; filename=review_report.xlsx"))
        .exceptionally(
            ex -> {
              log.error("Error generating Excel report: {}", ex.getMessage());
              return internalServerError("Error generating report");
            });
  }

  @BodyParser.Of(BodyParser.Json.class)
  public CompletionStage<Result> downloadStudentFeedbackReport(Http.Request request) {
    if (!authorizationService.isAuthorized(request, ALLOWED_ROLES)) {
      return CompletableFuture.completedFuture(unauthorized("Unauthorized access"));
    }

    try {
      JsonNode json = request.body().asJson();

      String studentName = json.get("studentName").asText();
      Long studentId = json.get("userId").asLong();
      String email = json.get("email").asText();
      String status = json.get("status").asText();
      float averageScore = (float) json.get("averageFeedbackScore").asDouble();

      ObjectMapper mapper = new ObjectMapper();
      List<FeedbackDTO> feedbacks =
          mapper.readerForListOf(FeedbackDTO.class).readValue(json.get("feedbacks"));

      return exportService
          .exportFeedbackForStudent(feedbacks, studentName, studentId, email, status, averageScore)
          .thenApply(
              fileBytes ->
                  ok(fileBytes)
                      .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                      .withHeader(
                          "Content-Disposition",
                          "attachment; filename="
                              + studentName.replace(" ", "_")
                              + "_feedback.xlsx"))
          .exceptionally(
              ex -> {
                log.error("Error generating student feedback report: {}", ex.getMessage());
                return internalServerError("Error generating student feedback report");
              });

    } catch (Exception e) {
      log.error("Error parsing student feedback JSON: {}", e.getMessage());
      return CompletableFuture.completedFuture(badRequest("Invalid request body"));
    }
  }
}
