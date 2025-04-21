package services.processors;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import models.dto.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.core.Repository;
import services.mappers.EntityMapper;
import services.processors.record.InputRecord;
import services.validations.Validations;

/**
 * FormProcessor is a generic class that processes form data of type T. It validates the syntax and
 * semantics of the input data, maps it to entities, and saves the processed data to a repository.
 *
 * @param <T> the type of entity to be processed
 */
public class FormProcessor<T> implements Processor<T, InputRecord> {

  private static final Logger log = LoggerFactory.getLogger(FormProcessor.class);
  private final Validations<T> validations;
  private final EntityMapper<T> entityMapper;
  private final Repository<T> repository;

  @Inject
  public FormProcessor(
      Validations<T> validations, EntityMapper<T> entityMapper, Repository<T> repository) {
    this.validations = validations;
    this.entityMapper = entityMapper;
    this.repository = repository;
  }

  /**
   * Processes the input record by validating its syntax and semantics, mapping it to entities, and
   * returning a list of valid entities.
   *
   * @param record the input record to be processed
   * @param context the context in which the processing is performed
   * @return a CompletableFuture containing a list of valid entities
   */
  @Override
  public CompletableFuture<List<T>> processData(InputRecord record, Context context) {
    return CompletableFuture.supplyAsync(
        () -> {
          if (!validations.validateSyntax(record)) {
            throw new IllegalArgumentException("Invalid form input: syntax validation failed");
          }

          List<T> entities = entityMapper.mapToEntityList(record, context);
          List<T> validEntities =
              entities.stream()
                  .filter(entity -> validations.validateSemantics(entity, repository))
                  .toList();

          if (validEntities.isEmpty()) {
            throw new IllegalStateException("Not a valid record. Data might already exist.");
          }

          return validEntities;
        });
  }

  /**
   * Saves the processed data to the repository. It returns a CompletableFuture containing the
   * status of the save operation.
   *
   * @param processedData the list of processed data to be saved
   * @param context the context in which the saving is performed
   * @return a CompletableFuture containing the status of the save operation
   */
  @Override
  public CompletableFuture<String> saveProcessedData(List<T> processedData, Context context) {
    return (CompletableFuture<String>)
        repository
            .saveAll(processedData, context)
            .thenApply(
                saveStatus -> {
                  int successCount = (int) saveStatus.get("successCount");
                  int failedCount = (int) saveStatus.get("failedCount");
                  List<String> failedRecords = (List<String>) saveStatus.get("failedRecords");

                  log.info(
                      "Successfully saved {} records. {} records failed.",
                      successCount,
                      failedCount);

                  if (failedCount > 0) {
                    log.warn("Some records failed to save: {}", failedRecords);
                    return "Partial success: "
                        + successCount
                        + " records saved, "
                        + failedCount
                        + " failed.";
                  }
                  return "CSV uploaded and processed successfully.";
                })
            .exceptionally(
                ex -> {
                  log.error("Error saving records: {}", ex.getMessage(), ex);
                  return "Failed to process CSV file.";
                });
  }
}
