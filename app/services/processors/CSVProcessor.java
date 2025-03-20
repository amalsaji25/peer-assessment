package services.processors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Result;
import play.mvc.Results;
import repository.Repository;
import services.mappers.EntityMapper;
import services.validations.Validations;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Singleton
public class CSVProcessor<T> implements FileProcessor {

    private final static Logger log = LoggerFactory.getLogger(CSVProcessor.class);
    private final Validations<T> validations;
    private final EntityMapper<T> entityMapper;
    private final Repository<T> repository;

    @Inject
    public CSVProcessor(Validations<T> validations, EntityMapper<T> entityMapper, Repository<T> repository) {
        this.validations = validations;
        this.entityMapper = entityMapper;
        this.repository = repository;
    }

    @Override
    public CompletionStage<Result> processFile(Path filePath, String fileType) {
        return CompletableFuture.supplyAsync(() -> {
            try (
                    Reader reader = Files.newBufferedReader(filePath);
                    CSVParser csvParser = CSVParser.parse(reader,
                            CSVFormat.Builder.create()
                                    .setHeader()
                                    .setSkipHeaderRecord(true)
                                    .setIgnoreHeaderCase(true)
                                    .setTrim(true)
                                    .get())
            ) {
                // Validate CSV Header Order
                List<String> actualHeaders = new ArrayList<>(csvParser.getHeaderNames());
                if (!validations.validateFieldOrder(actualHeaders)) {
                    log.warn("CSV file has incorrect field order.");
                    return CompletableFuture.completedFuture(Results.badRequest("CSV file has incorrect field order."));
                }

                // Perform Syntax Validation & Map to Entities
                List<T> syntaxValidRecords = csvParser.getRecords().stream()
                        .filter(validations::validateSyntax)
                        .map(entityMapper::mapToEntity)
                        .toList();

                log.info("Syntax validation complete. Valid records count: {}", syntaxValidRecords.size());

                // Perform Semantic Validation
                List<T> semanticValidRecords = syntaxValidRecords.stream()
                        .filter(record -> validations.validateSemantics(record, repository))
                        .toList();

                log.info("Semantic validation complete. Valid records count: {}", semanticValidRecords.size());

                if (semanticValidRecords.isEmpty()) {
                    log.warn("No valid records found after semantic validation.");
                    return CompletableFuture.completedFuture(Results.badRequest("No valid records found. Check logs for details."));
                }

                // Return async save operation with `thenCompose()`
                return repository.saveAll(semanticValidRecords).thenApply(saveStatus -> {
                    int successCount = (int) saveStatus.get("successCount");
                    int failedCount = (int) saveStatus.get("failedCount");
                    List<String> failedRecords = (List<String>) saveStatus.get("failedRecords");

                    log.info("Successfully saved {} records. {} records failed.", successCount, failedCount);

                    if (failedCount > 0) {
                        log.warn("Some records failed to save: {}", failedRecords);
                        return Results.ok("Partial success: " + successCount + " records saved, " + failedCount + " failed.");
                    }
                    return Results.ok("CSV uploaded and processed successfully.");
                }).exceptionally(ex -> {
                    log.error("Error saving records: {}", ex.getMessage(), ex);
                    return Results.internalServerError("Failed to process CSV file.");
                });

            } catch (Exception e) {
                log.error("Error processing CSV file: {}", e.getMessage(), e);
                return CompletableFuture.completedFuture(Results.internalServerError("Error processing CSV file."));
            }
        }).thenCompose(result -> result);
    }
}
