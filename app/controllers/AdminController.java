package controllers;

import play.mvc.*;
import services.AdminAuthenticatorService;
import services.FileUploadService;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;



@Security.Authenticated(AdminAuthenticatorService.class)
@Singleton
public class AdminController extends Controller {

    private final FileUploadService fileUploadService;

    @Inject
    public AdminController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @BodyParser.Of(BodyParser.MultipartFormData.class)
    public CompletionStage<Result> uploadFile(Http.Request request) {
        Http.MultipartFormData<play.libs.Files.TemporaryFile> body = request.body().asMultipartFormData();
        Optional<Http.MultipartFormData.FilePart<play.libs.Files.TemporaryFile>> filePart = Optional.ofNullable(body.getFile("file"));

        String fileType = request.queryString("fileType")
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
        return fileUploadService.processFileUpload(uploadedFile.toPath().toFile(), fileType)
                .thenApply(result -> result.status() == 200 ? ok("File uploaded successfully") : badRequest("File processing failed"));
    }

}
