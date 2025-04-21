package services.validations;

import java.util.List;
import javax.inject.Singleton;
import models.Enrollment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.core.Repository;
import services.processors.record.InputRecord;

/**
 * EnrollmentValidation is a singleton class that implements the Validations interface for
 * validating enrollment records. It checks for syntax, semantics, and field order of enrollment
 * data.
 */
@Singleton
public class EnrollmentValidation implements Validations<Enrollment> {

  private static final Logger log = LoggerFactory.getLogger(EnrollmentValidation.class);
  private static final List<String> MANDATORY_FIELDS =
      List.of("student_id", "course_code", "course_section", "term");
  private static final List<String> EXPECTED_FIELDS_ORDER =
      List.of("student_id", "course_code", "course_section", "term");

  /**
   * Validates the syntax of the enrollment record by checking for missing or empty mandatory
   * fields.
   *
   * @param record the input record to be validated
   * @return true if the syntax is valid, false otherwise
   */
  @Override
  public boolean validateSyntax(InputRecord record) {
    boolean isValidSyntax =
        MANDATORY_FIELDS.stream()
            .allMatch(field -> record.isMapped(field) && !record.get(field).isEmpty());

    if (!isValidSyntax) {
      log.warn("Skipping invalid enrollment record due to missing mandatory fields: {}", record);
    }
    return isValidSyntax;
  }

  /**
   * Validates the semantics of the enrollment record by checking for existing records and valid
   * course and student IDs.
   *
   * @param record the enrollment record to be validated
   * @param repository the repository to check for existing records
   * @return true if the semantics are valid, false otherwise
   */
  @Override
  public boolean validateSemantics(Enrollment record, Repository<Enrollment> repository) {
    // Ensure student exists
    if (record.getStudent() == null) {
      log.warn("Invalid student ID: {} for enrollment", record.getStudent());
      return false;
    }

    // Ensure course exists
    if (record.getCourse() == null) {
      log.warn("Invalid course ID: {} for enrollment", record.getCourse());
      return false;
    }

    return true;
  }

  /**
   * Validates the field order of the enrollment record by comparing it with the expected order.
   *
   * @param actualHeaders the actual headers of the CSV file
   * @return true if the field order is correct, false otherwise
   */
  @Override
  public boolean validateFieldOrder(List<String> actualHeaders) {
    boolean isCorrectOrder = actualHeaders.equals(EXPECTED_FIELDS_ORDER);
    if (!isCorrectOrder) {
      log.warn(
          "File has incorrect field order. Expected order is {}, Found order is {}",
          EXPECTED_FIELDS_ORDER,
          actualHeaders);
    }
    return isCorrectOrder;
  }
}
