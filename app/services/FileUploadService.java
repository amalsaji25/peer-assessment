package services;

import exceptions.InvalidFileUploadException;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.dto.Context;
import models.enums.FileType;
import services.processors.Processor;
import services.processors.ProcessorStrategy;

/**
 * FileUploadService is a service class that handles file upload processing. It provides methods to
 * validate, parse, and process files based on their type.
 */
@Singleton
public class FileUploadService {

  private final ProcessorStrategy processorStrategy;

  @Inject
  public FileUploadService(ProcessorStrategy processorStrategy) {
    this.processorStrategy = processorStrategy;
  }

  /**
   * Retrieves a file processor based on the file type. It validates the file format and returns a
   * CompletableFuture containing the appropriate processor.
   *
   * @param filePath the path to the file
   * @param fileType the type of the file (e.g., users, courses, enrollments)
   * @return a CompletableFuture containing the processor for the specified file type
   */
  public <T> CompletableFuture<Processor<T, Path>> getFileProcessor(
      File filePath, String fileType) {
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

  /**
   * Parses and processes the file using the specified processor. It returns a CompletableFuture
   * containing the processed data.
   *
   * @param processor the processor to use for parsing and processing the file
   * @param file the file to be processed
   * @param context the context in which the processing is performed
   * @return a CompletableFuture containing the processed data
   */
  public <T> CompletableFuture<List<T>> parseAndProcessFile(
      Processor<T, Path> processor, File file, Context context) {
    Path filePath = file.toPath();
    return processor.processData(filePath, context);
  }

  /**
   * Saves the processed file data using the specified processor. It returns a CompletableFuture
   * containing the result of the save operation.
   *
   * @param processor the processor to use for saving the processed data
   * @param processedData the processed data to be saved
   * @param context the context in which the saving is performed
   * @return a CompletableFuture containing the result of the save operation
   */
  public <T> CompletableFuture<String> saveProcessedFileData(
      Processor<T, Path> processor, List<T> processedData, Context context) {
    return processor.saveProcessedData(processedData, context);
  }

  /**
   * Validates the file format. It checks if the file has a valid extension (e.g., .csv).
   *
   * @param file the file to be validated
   * @return true if the file has a valid format, false otherwise
   */
  private boolean isValidFile(File file) {
    return FileType.isValidFileExtension(file.getName());
  }
}
