package services;

import models.enums.FileType;
import services.processors.FileProcessor;
import play.mvc.Result;
import services.processors.FileProcessorStrategy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Singleton
public class FileUploadService {

    private final FileProcessorStrategy fileProcessorStrategy;

    @Inject
    public FileUploadService(FileProcessorStrategy fileProcessorStrategy) {
        this.fileProcessorStrategy = fileProcessorStrategy;
    }

    public CompletionStage<Result> processFileUpload(File filePath, String fileType) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isValidFile(filePath)) {
                return CompletableFuture.completedFuture(play.mvc.Results.badRequest("Invalid file format. Only CSV files are allowed."));
            }

            FileProcessor fileProcessor = fileProcessorStrategy.getProcessor(fileType);
            if (fileProcessor == null) {
                return CompletableFuture.completedFuture(play.mvc.Results.badRequest("Invalid fileType. Allowed: users, courses, enrollments."));
            }

            return fileProcessor.processFile(filePath.toPath(), fileType);
        }).thenCompose(result -> result);
    }

    private boolean isValidFile(File file) {
        return FileType.isValidFileExtension(file.getName());
    }
}