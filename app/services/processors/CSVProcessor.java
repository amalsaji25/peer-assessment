package services.processors;

import exceptions.InvalidCsvException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.dto.Context;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.core.Repository;
import services.mappers.EntityMapper;
import services.processors.record.CSVInputRecord;
import services.validations.Validations;

/**
 * CSVProcessor is a singleton class that processes CSV files. It validates the syntax and semantics
 * of the records, maps them to entities, and saves them to the database. It uses Validations,
 * EntityMapper, and Repository interfaces for validation, mapping, and persistence respectively.
 *
 * @param <T> the type of entity to be processed
 */
@Singleton
public class CSVProcessor<T> implements Processor<T, Path> {

  private static final Logger log = LoggerFactory.getLogger(CSVProcessor.class);
  private final Validations<T> validations;
  private final EntityMapper<T> entityMapper;
  private final Repository<T> repository;

  @Inject
  public CSVProcessor(
      Validations<T> validations, EntityMapper<T> entityMapper, Repository<T> repository) {
    this.validations = validations;
    this.entityMapper = entityMapper;
    this.repository = repository;
  }

  /**
   * Processes the CSV file by validating its syntax and semantics, mapping it to entities, and
   * saving the entities to the database.
   *
   * @param filePath the path of the CSV file to be processed
   * @param context the context in which the processing is performed
   * @return a CompletableFuture containing a list of processed entities
   */
  @Override
  public CompletableFuture<List<T>> processData(Path filePath, Context context) {
    return CompletableFuture.supplyAsync(
        () -> {
          CSVParser csvParser;
          try (Reader reader = Files.newBufferedReader(filePath)) {
            csvParser =
                CSVParser.parse(
                    reader,
                    CSVFormat.Builder.create()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .setIgnoreHeaderCase(true)
                        .setTrim(true)
                        .get());

            // Special Case for ReviewTaskEntityMapper handling 2nd variant for group file data
            if (entityMapper.getClass().getSimpleName().equals("ReviewTaskEntityMapper")
                && csvParser.getHeaderNames().size() == 5) {
              csvParser = getCsvRecordsForReviewTasks(csvParser);
            }

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
          } catch (InvalidCsvException e) {
            log.error("Invalid CSV content: {}", e.getMessage());
            throw e;
          } catch (Exception e) {
            log.error("Unexpected error processing CSV file: {}", e.getMessage(), e);
            throw new InvalidCsvException("Unexpected error while processing CSV file.", e);
          }
        });
  }

  /**
   * Transforms the CSV records for review tasks by grouping them and creating a new CSV format.
   *
   * @param csvParser the original CSV parser
   * @return a new CSV parser with transformed records
   * @throws IOException if an I/O error occurs
   */
  private CSVParser getCsvRecordsForReviewTasks(CSVParser csvParser) throws IOException {
    if (entityMapper.getClass().getSimpleName().equals("ReviewTaskEntityMapper")
        && csvParser.getHeaderNames().size() == 5) {
      Map<String, List<CSVInputRecord>> groupedRecords =
          csvParser.getRecords().stream()
              .map(CSVInputRecord::new)
              .collect(Collectors.groupingBy(record -> record.get("Group").trim()));

      int maxGroupSize = groupedRecords.values().stream().mapToInt(List::size).max().orElse(0);

      List<String> headers = new ArrayList<>();
      headers.add("Group ID");
      headers.add("Group Name");
      headers.add("Group Size");
      headers.add("Group Description");
      headers.add("Assigned teacher Username");
      headers.add("Assigned teacher Firstname");
      headers.add("Assigned teacher Lastname");
      headers.add("Assigned teacher Email");

      for (int i = 1; i <= maxGroupSize; i++) {
        headers.add("Member " + i + " Username");
        headers.add("Member " + i + " ID Number");
        headers.add("Member " + i + " Firstname");
        headers.add("Member " + i + " Lastname");
        headers.add("Member " + i + " Email");
      }
      Path tempFile = Files.createTempFile("transformed-review-task", ".csv");
      BufferedWriter writer = Files.newBufferedWriter(tempFile);
      CSVFormat format =
          CSVFormat.Builder.create()
              .setHeader(headers.toArray(new String[0]))
              .setSkipHeaderRecord(false)
              .get();
      CSVPrinter csvPrinter = new CSVPrinter(writer, format);

      AtomicLong counter = new AtomicLong(0);
      groupedRecords.values().stream()
          .filter(group -> !"No group".equals(group.get(0).get("Group").trim()))
          .forEach(
              group -> {
                List<String> row = new ArrayList<>();
                long baseTime = System.currentTimeMillis();
                long groupId = baseTime + counter.getAndIncrement();
                row.add(String.valueOf(groupId));
                row.add(group.get(0).get("Group").trim());
                row.add(String.valueOf(group.size()));
                row.add("");
                row.add("");
                row.add("");
                row.add("");
                row.add("");

                for (int i = 0; i < headers.size() - 4; i += 4) {
                  int memberIndex = (i / 4) + 1;
                  if (memberIndex <= group.size()) {
                    CSVInputRecord member = group.get(memberIndex - 1);
                    row.add(""); // Username placeholder
                    row.add(member.get("ID number").trim());
                    row.add(member.get("First name").trim());
                    row.add(member.get("Last name").trim());
                    row.add(""); // Email placeholder
                  } else {
                    row.add("");
                    row.add("");
                    row.add("");
                    row.add("");
                  }
                }

                try {
                  csvPrinter.printRecord(row);
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }
              });

      csvPrinter.flush();
      csvPrinter.close();
      writer.close();

      Reader transformedReader = Files.newBufferedReader(tempFile);
      csvParser =
          CSVParser.parse(
              transformedReader,
              CSVFormat.Builder.create()
                  .setHeader()
                  .setSkipHeaderRecord(true)
                  .setIgnoreHeaderCase(true)
                  .setTrim(true)
                  .get());
    }
    return csvParser;
  }

  /**
   * Saves the processed data to the database. It handles the response and logs the status of the
   * save operation.
   *
   * @param processedData the list of processed entities to be saved
   * @param context the context in which the saving is performed
   * @return a CompletableFuture containing a message indicating the result of the save operation
   */
  @Override
  public CompletableFuture<String> saveProcessedData(List<T> processedData, Context context) {
    return (CompletableFuture<String>)
        repository
            .saveAll(processedData, context)
            .thenApply(
                saveStatus -> {
                  int successCount =
                      Optional.ofNullable((Integer) saveStatus.get("successCount")).orElse(0);
                  int skippedCount =
                      Optional.ofNullable((Integer) saveStatus.get("failedCount")).orElse(0);
                  List<String> skippedRecords = (List<String>) saveStatus.get("failedRecords");

                  log.info(
                      "Successfully saved {} records. Skipped {} already existing records.",
                      successCount,
                      skippedCount);

                  if (skippedCount > 0) {
                    log.warn("Some records failed to save: {}", skippedRecords);
                    return "Upload completed: "
                        + successCount
                        + " new records added, "
                        + skippedCount
                        + " duplicate records skipped.";
                  }
                  return "Upload completed successfully";
                })
            .exceptionally(
                ex -> {
                  log.error("Error saving records: {}", ex.getMessage(), ex);
                  return "An error occurred while saving the records.";
                });
  }
}
