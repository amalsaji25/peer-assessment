package controllers;

import static play.mvc.Results.*;

import java.io.File;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.enums.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Files;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import services.AuthenticationService;
import services.AuthorizationService;
import services.FileUploadService;

@Security.Authenticated(AuthenticationService.class)
@Singleton
public class FileUploadController {

  private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);
  private static final Set<Roles> ALLOWED_ROLES = Set.of(Roles.ADMIN);
  private final FileUploadService fileUploadService;
  private final AuthorizationService authorizationService;

  @Inject
  public FileUploadController(
      FileUploadService fileUploadService, AuthorizationService authorizationService) {
    this.fileUploadService = fileUploadService;
    this.authorizationService = authorizationService;
  }

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

    // Process the uploaded file
    return fileUploadService
        .getFileProcessor(uploadedFile, fileType)
        .thenCompose(
            fileProcessor ->
                fileUploadService
                    .parseAndProcessFile(fileProcessor, uploadedFile)
                    .thenCompose(
                        processedData ->
                            fileUploadService.saveProcessedFileData(fileProcessor, processedData)))
        .thenApply(result -> ok(result).withSession(AuthenticationService.updateSession(request)))
        .exceptionally(
            e -> {
              log.error("File processing failed with error: {}", e.getMessage());
              return internalServerError("File processing failed with error: " + e.getMessage())
                  .withSession(AuthenticationService.updateSession(request));
            });
  }
}
