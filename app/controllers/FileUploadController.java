package controllers;

import static play.mvc.Results.*;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.dto.Context;
import models.enums.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Files;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import repository.core.CourseRepository;
import services.AuthenticationService;
import services.AuthorizationService;
import services.FileUploadService;

/** Controller for handling file upload requests, including processing and saving uploaded files. */
@Security.Authenticated(AuthenticationService.class)
@Singleton
public class FileUploadController {

  private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);
  private static final Set<Roles> ALLOWED_ROLES = Set.of(Roles.ADMIN, Roles.PROFESSOR);
  private final FileUploadService fileUploadService;
  private final AuthorizationService authorizationService;
  private final CourseRepository courseRepository;

  @Inject
  public FileUploadController(
      FileUploadService fileUploadService,
      AuthorizationService authorizationService,
      CourseRepository courseRepository) {
    this.fileUploadService = fileUploadService;
    this.authorizationService = authorizationService;
    this.courseRepository = courseRepository;
  }

  /**
   * Handles the file upload request, processes the uploaded file, and saves the processed data.
   *
   * @param request The HTTP request containing the file and form data.
   * @return A CompletionStage containing the Result of the upload operation.
   */
  @BodyParser.Of(BodyParser.MultipartFormData.class)
  public CompletionStage<Result> uploadFile(Http.Request request) {
    String sessionId = request.session().get("userId").orElse("NONE");
    log.info("Session ID: {}", sessionId);
    if (!authorizationService.isAuthorized(request, ALLOWED_ROLES)) {
      return CompletableFuture.completedFuture(unauthorized("Unauthorized access"));
    }
    Http.MultipartFormData<play.libs.Files.TemporaryFile> body =
        request.body().asMultipartFormData();
    Optional<Http.MultipartFormData.FilePart<Files.TemporaryFile>> filePart =
        Optional.ofNullable(body.getFile("file"));

    String fileType =
        request
            .queryString("fileType")
            .orElseThrow(() -> new IllegalArgumentException("fileType is required"));

    if (filePart.isEmpty()) {
      return CompletableFuture.completedFuture(badRequest("Missing file"));
    }

    // Create the upload directory if it doesn't exist
    File uploadDir = new File("uploads");
    if (!uploadDir.exists()) {
      uploadDir.mkdirs();
    }

    // Get the uploaded file part
    Http.MultipartFormData.FilePart<play.libs.Files.TemporaryFile> part = filePart.get();

    // Copy the temporary file to the upload directory
    play.libs.Files.TemporaryFile temporaryFile = part.getRef();
    File uploadedFile = new File(uploadDir, part.getFilename());
    temporaryFile.copyTo(uploadedFile.toPath(), true);

    // Map non-file form data
    Map<String, String[]> formData = body.asFormUrlEncoded();
    Context context = new Context();
    if (formData != null) {
      formData.forEach(
          (key, value) -> {
            if (key.equals("courseCode")) {
              String courseCode = value[0].split(":::")[0].trim();
              context.setCourseCode(courseCode);
              String courseSection = value[0].split(":::")[1].trim();
              context.setCourseSection(courseSection);
              String term = value[0].split(":::")[2].trim();
              context.setTerm(term);
            }
          });
    }
    // Process the uploaded file
    return fileUploadService
        .getFileProcessor(uploadedFile, fileType)
        .thenCompose(
            fileProcessor ->
                fileUploadService
                    .parseAndProcessFile(fileProcessor, uploadedFile, context)
                    .thenCompose(
                        processedData ->
                            fileUploadService.saveProcessedFileData(
                                fileProcessor, processedData, context)))
        .thenApply(
            result -> {
              if (fileType.equals("review_tasks")) {
                courseRepository.updateIsPeerAssignedFlagForCourse(
                    context.getCourseCode(), context.getCourseSection(), context.getTerm());
              }
              ObjectNode successJson = Json.newObject();
              successJson.put("success", result);
              return ok(successJson).withSession(AuthenticationService.updateSession(request));
            })
        .exceptionally(
            e -> {
              log.error("File processing failed with error: {}", e.getMessage());
              Throwable cause = e;
              while (cause.getCause() != null) {
                cause = cause.getCause();
              }
              String userMessage =
                  cause.getMessage() != null ? cause.getMessage() : "An unexpected error occurred.";

              ObjectNode errorJson = Json.newObject();
              errorJson.put("error", userMessage);
              return internalServerError(errorJson)
                  .withSession(AuthenticationService.updateSession(request));
            });
  }
}
