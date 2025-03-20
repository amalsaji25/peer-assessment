package controllers;

import models.enums.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.*;
import services.AuthenticationService;
import services.AuthorizationService;
import services.FileUploadService;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.io.File;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static models.enums.Roles.ADMIN;


@Security.Authenticated(AuthenticationService.class)
@Singleton
public class AdminController extends Controller {

    private final FileUploadService fileUploadService;

    private final AuthorizationService authorizationService;

    private static final Set<Roles> ALLOWED_ROLES = Set.of(ADMIN);

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Inject
    public AdminController(FileUploadService fileUploadService, AuthorizationService authorizationService) {
        this.fileUploadService = fileUploadService;
        this.authorizationService = authorizationService;
    }

    public Result dashboard(Http.Request request){
        return ok(views.html.dashboard.render("Admin Dashboard"));
    }

    @BodyParser.Of(BodyParser.MultipartFormData.class)
    public CompletionStage<Result> uploadFile(Http.Request request) {
        String sessionId = request.session().get("userId").orElse("NONE");
        log.info("Session ID: {}", sessionId);
        if(!authorizationService.isAuthorized(request, ALLOWED_ROLES)){
            return CompletableFuture.completedFuture(unauthorized("Unauthorized access"));
        }
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
                .thenApply(result -> result.status() == 200 ? ok("File uploaded successfully")
                        .withSession(AuthenticationService.updateSession(request))
                        : badRequest("File processing failed")
                        .withSession(AuthenticationService.updateSession(request)));
    }

}
