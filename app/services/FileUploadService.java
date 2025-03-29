package services;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;

import exceptions.InvalidFileUploadException;
import models.enums.FileType;
import services.processors.FileProcessor;
import services.processors.FileProcessorStrategy;

@Singleton
public class FileUploadService {

  private final FileProcessorStrategy fileProcessorStrategy;

  @Inject
  public FileUploadService(FileProcessorStrategy fileProcessorStrategy) {
    this.fileProcessorStrategy = fileProcessorStrategy;
  }

  public <T> CompletableFuture<FileProcessor<T>> getFileProcessor(File filePath, String fileType) {
    return CompletableFuture.supplyAsync(
        () -> {
          if (!isValidFile(filePath)) {
            throw new InvalidFileUploadException(
                "Invalid file format. Only CSV files are allowed.");
          }

          FileProcessor<T> fileProcessor = fileProcessorStrategy.getProcessor(fileType);
          if (fileProcessor == null) {
            throw new InvalidFileUploadException(
                "Invalid fileType. Allowed: users, courses, enrollments.");
          }

          return fileProcessor;
        });
  }

  public <T> CompletableFuture<List<T>> parseAndProcessFile(
      FileProcessor<T> fileProcessor, File file) {
    Path filePath = file.toPath();
    return fileProcessor.parseAndProcessFile(filePath);
  }

  public <T> CompletableFuture<String> saveProcessedFileData(
      FileProcessor<T> fileProcessor, List<T> processedData) {
    return fileProcessor.saveProcessedFileData(processedData);
  }

  private boolean isValidFile(File file) {
    return FileType.isValidFileExtension(file.getName());
  }
}
