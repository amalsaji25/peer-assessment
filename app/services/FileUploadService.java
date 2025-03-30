package services;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;

import exceptions.InvalidFileUploadException;
import models.dto.Context;
import models.enums.FileType;
import services.processors.Processor;
import services.processors.ProcessorStrategy;

@Singleton
public class FileUploadService {

  private final ProcessorStrategy processorStrategy;

  @Inject
  public FileUploadService(ProcessorStrategy processorStrategy) {
    this.processorStrategy = processorStrategy;
  }

  public <T> CompletableFuture<Processor<T,Path>> getFileProcessor(File filePath, String fileType) {
    return CompletableFuture.supplyAsync(
        () -> {
          if (!isValidFile(filePath)) {
            throw new InvalidFileUploadException(
                "Invalid file format. Only CSV files are allowed.");
          }

          Processor<T, Path> processor = processorStrategy.getFileProcessor(fileType);
          if (processor == null) {
            throw new InvalidFileUploadException(
                "Invalid fileType. Allowed: users, courses, enrollments.");
          }

          return processor;
        });
  }

  public <T> CompletableFuture<List<T>> parseAndProcessFile(
          Processor<T,Path> processor, File file, Context context) {
    Path filePath = file.toPath();
    return processor.processData(filePath, context);
  }

  public <T> CompletableFuture<String> saveProcessedFileData(
          Processor<T,Path> processor, List<T> processedData, Context context) {
    return processor.saveProcessedData(processedData, context);
  }

  private boolean isValidFile(File file) {
    return FileType.isValidFileExtension(file.getName());
  }
}
