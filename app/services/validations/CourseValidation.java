package services.validations;

import java.util.List;
import javax.inject.Singleton;
import models.Course;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.core.CourseRepository;
import repository.core.Repository;
import services.processors.record.InputRecord;

/**
 * CourseValidation is a singleton class that implements the Validations interface for validating
 * course records. It checks for syntax, semantics, and field order of course data.
 */
@Singleton
public class CourseValidation implements Validations<Course> {

  private static final Logger log = LoggerFactory.getLogger(CourseValidation.class);
  private static final List<String> MANDATORY_FIELDS =
      List.of("course_code", "course_name", "course_section", "professor_id", "term");
  private static final List<String> EXPECTED_FIELDS_ORDER =
      List.of("course_code", "course_name", "course_section", "professor_id", "term");

  /**
   * Validates the syntax of the course record by checking for missing or empty mandatory fields.
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
      log.warn("Skipping invalid course record due to missing mandatory fields: {}", record);
    }
    return isValidSyntax;
  }

  /**
   * Validates the semantics of the course record by checking for existing records and valid course
   * and professor IDs.
   *
   * @param record the course record to be validated
   * @param repository the repository to check for existing records
   * @return true if the semantics are valid, false otherwise
   */
  @Override
  public boolean validateSemantics(Course record, Repository<Course> repository) {
    CourseRepository courseRepository = (CourseRepository) repository;

    // Check if the course already exists
    if (courseRepository
        .findByCourseCodeAndSectionAndTerm(
            record.getCourseCode(), record.getCourseSection(), record.getTerm())
        .isPresent()) {
      log.warn("Course {} already exists in the database", record.getCourseCode());
      return false;
    }

    // Ensure professor exists
    if (record.getProfessor() == null) {
      log.warn("Professor ID is invalid or does not exist for course: {}", record.getCourseCode());
      return false;
    }

    return true;
  }

  /**
   * Validates the field order of the course record by checking if it matches the expected order.
   *
   * @param actualHeaders the actual headers of the input record
   * @return true if the field order is valid, false otherwise
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
