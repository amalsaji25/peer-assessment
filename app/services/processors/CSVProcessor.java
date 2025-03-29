package services.processors;

import exceptions.InvalidCsvException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.core.Repository;
import services.mappers.EntityMapper;
import services.validations.Validations;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Singleton
public class CSVProcessor<T> implements FileProcessor<T> {

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
    public CompletableFuture<List<T>> parseAndProcessFile(Path filePath) {
    return CompletableFuture.supplyAsync(
        () -> {
          try (Reader reader = Files.newBufferedReader(filePath);
              CSVParser csvParser =
                  CSVParser.parse(
                      reader,
                      CSVFormat.Builder.create()
                          .setHeader()
                          .setSkipHeaderRecord(true)
                          .setIgnoreHeaderCase(true)
                          .setTrim(true)
                          .get())) {

            // Validate CSV Header Order
            List<String> actualHeaders = new ArrayList<>(csvParser.getHeaderNames());
            if (!validations.validateFieldOrder(actualHeaders)) {
              log.warn("CSV file has incorrect field order.");
              throw new InvalidCsvException("CSV file has incorrect field order.");
            }

            // Perform Syntax Validation & Map to Entities
            List<T> syntaxValidRecords =
                csvParser.getRecords().stream()
                    .filter(validations::validateSyntax)
                    .flatMap(record -> entityMapper.mapToEntityList(record).stream())// For records (e.g., users, courses), mapToEntityList will return a single-element list, but for record like ReviewTask, it will return a list of ReviewTask objects
                    .toList();

            log.info(
                "Syntax validation complete. Valid records count: {}", syntaxValidRecords.size());

            // Perform Semantic Validation
            List<T> semanticValidRecords =
                syntaxValidRecords.stream()
                    .filter(record -> validations.validateSemantics(record, repository))
                    .toList();

            log.info(
                "Semantic validation complete. Valid records count: {}",
                semanticValidRecords.size());

            if (semanticValidRecords.isEmpty()) {
              log.warn("No valid records found after semantic validation.");
              throw new InvalidCsvException("No valid records found after semantic validation.");
            }
            return semanticValidRecords;
          }catch (InvalidCsvException e) {
              log.error("Invalid CSV content: {}", e.getMessage());
              throw e;
          } catch (Exception e) {
              log.error("Unexpected error processing CSV file: {}", e.getMessage(), e);
              throw new InvalidCsvException("Unexpected error while processing CSV file.", e);
          }
        });
    }

    @Override
    public CompletableFuture<String> saveProcessedFileData(List<T> processedData) {
                return (CompletableFuture<String>) repository.saveAll(processedData).thenApply(saveStatus -> {
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
