package services.processors;

import models.dto.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.core.Repository;
import services.mappers.EntityMapper;
import services.processors.record.InputRecord;
import services.validations.Validations;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FormProcessor<T> implements Processor<T, InputRecord> {

    private final Validations<T> validations;
    private final EntityMapper<T> entityMapper;
    private final Repository<T> repository;
    private static final Logger log = LoggerFactory.getLogger(FormProcessor.class);

    @Inject
    public FormProcessor(Validations<T> validations, EntityMapper<T> entityMapper, Repository<T> repository) {
        this.validations = validations;
        this.entityMapper = entityMapper;
        this.repository = repository;
    }

    @Override
    public CompletableFuture<List<T>> processData(InputRecord record, Context context) {
        return CompletableFuture.supplyAsync(() -> {
            if (!validations.validateSyntax(record)) {
                throw new IllegalArgumentException("Invalid form input: syntax validation failed");
            }

            List<T> entities = entityMapper.mapToEntityList(record,context);
            List<T> validEntities = entities.stream()
                    .filter(entity -> validations.validateSemantics(entity, repository))
                    .toList();

            if (validEntities.isEmpty()) {
                throw new IllegalStateException("Not a valid record. Data might already exist.");
            }

            return validEntities;
        });
    }

    @Override
    public CompletableFuture<String> saveProcessedData(List<T> processedData, Context context) {
        return (CompletableFuture<String>) repository.saveAll(processedData, context).thenApply(saveStatus -> {
            int successCount = (int) saveStatus.get("successCount");
            int failedCount = (int) saveStatus.get("failedCount");
            List<String> failedRecords = (List<String>) saveStatus.get("failedRecords");

            log.info("Successfully saved {} records. {} records failed.", successCount, failedCount);

            if (failedCount > 0) {
                log.warn("Some records failed to save: {}", failedRecords);
                return "Partial success: " + successCount + " records saved, " + failedCount + " failed.";
            }
            return "CSV uploaded and processed successfully.";
        }).exceptionally(ex -> {
            log.error("Error saving records: {}", ex.getMessage(), ex);
            return "Failed to process CSV file.";
        });
    }
}
