package services.validations;

import java.util.List;
import java.util.regex.Pattern;
import javax.inject.Singleton;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.core.Repository;
import repository.core.UserRepository;
import services.processors.record.InputRecord;

/**
 * UserValidation is a singleton class that implements the Validations interface for validating user
 * records. It checks for syntax, semantics, and field order of user data.
 */
@Singleton
public class UserValidation implements Validations<User> {

  private static final Logger log = LoggerFactory.getLogger(UserValidation.class);
  private static final List<String> mandatoryFields =
      List.of("user_id", "email", "first_name", "last_name", "role");
  private static final List<String> expectedFieldsOrder =
      List.of("user_id", "email", "first_name", "last_name", "role");
  private static final List<String> ALLOWED_ROLES = List.of("student", "professor");
  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

  /**
   * Validates the syntax of the user record by checking for missing or empty mandatory fields.
   *
   * @param record the input record to be validated
   * @return true if the syntax is valid, false otherwise
   */
  @Override
  public boolean validateSyntax(InputRecord record) {
    List<String> missingFields =
        mandatoryFields.stream()
            .filter(field -> !record.isMapped(field) || record.get(field).isEmpty())
            .toList();

    if (!missingFields.isEmpty()) {
      log.warn(
          "Skipping invalid user record due to missing or empty fields: {} | Record: {}",
          missingFields,
          record);
      return false;
    }
    return true;
  }

  /**
   * Validates the semantics of the user record by checking for existing records and valid email
   * format.
   *
   * @param record the user record to be validated
   * @param repository the repository to check for existing records
   * @return true if the semantics are valid, false otherwise
   */
  @Override
  public boolean validateSemantics(User record, Repository<User> repository) {
    UserRepository userRepository = (UserRepository) repository;

    if (userRepository.findById(record.getUserId()).isPresent()) {
      log.warn("User {} already exists", record.getUserId());
      return false;
    }

    if (!EMAIL_PATTERN.matcher(record.getEmail()).matches()) {
      log.warn(
          "Invalid email format for user: {} with email given as : {}",
          record.getUserId(),
          record.getEmail());
      return false;
    }

    if (!ALLOWED_ROLES.contains(record.getRole().toLowerCase())) {
      log.warn(
          "Invalid user role for user ID: {} - Role: {}", record.getUserId(), record.getRole());
      return false;
    }

    return true;
  }

  /**
   * Validates the field order of the user record by checking if it matches the expected order.
   *
   * @param actualHeaders the actual headers from the CSV file
   * @return true if the field order is correct, false otherwise
   */
  @Override
  public boolean validateFieldOrder(List<String> actualHeaders) {
    boolean isCorrectOrder = actualHeaders.equals(expectedFieldsOrder);
    if (!isCorrectOrder) {
      log.warn(
          "File has incorrect field order. Expected order is {}, Found order is {}",
          expectedFieldsOrder,
          actualHeaders);
    }
    return isCorrectOrder;
  }
}
