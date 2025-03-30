package services.processors;

import exceptions.InvalidCsvException;
import models.dto.Context;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.core.Repository;
import services.mappers.EntityMapper;
import services.processors.record.CSVInputRecord;
import services.validations.Validations;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
public class CSVProcessor<T> implements Processor<T, Path> {

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
    public CompletableFuture<List<T>> processData(Path filePath, Context context) {
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
                              .map(CSVInputRecord::new)
                              .peek(parsed -> log.info("Syntax Processing record: {}", parsed))
                              .filter(validations::validateSyntax)
                              .peek(parsed -> log.info("Mapping Syntax valid record: {}", parsed))
                              .flatMap(parsed -> entityMapper.mapToEntityList(parsed, context).stream())
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
              throw new InvalidCsvException("No valid records found. Data might already exist.");
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
    public CompletableFuture<String> saveProcessedData(List<T> processedData, Context context) {
                return (CompletableFuture<String>) repository.saveAll(processedData, context).thenApply(saveStatus -> {
                    int successCount = Optional.ofNullable((Integer) saveStatus.get("successCount")).orElse(0);
                    int skippedCount = Optional.ofNullable((Integer) saveStatus.get("failedCount")).orElse(0);
                    List<String> skippedRecords = (List<String>) saveStatus.get("failedRecords");

                    log.info("Successfully saved {} records. Skipped {} already existing records.", successCount, skippedCount);

                    if (skippedCount > 0) {
                        log.warn("Some records failed to save: {}", skippedRecords);
                        return "Upload completed: " + successCount + " new records added, " + skippedCount + " duplicate records skipped.";
                    }
                    return "Upload completed successfully";
                }).exceptionally(ex -> {
                    log.error("Error saving records: {}", ex.getMessage(), ex);
                    return "An error occurred while saving the records.";
                });
        }
}
