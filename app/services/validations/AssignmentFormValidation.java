package services.validations;

import forms.AssignmentForm;
import java.time.LocalDate;
import java.util.List;
import models.ReviewTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.core.Repository;
import services.processors.record.InputRecord;

/**
 * AssignmentFormValidation is a class that implements the Validations interface for validating
 * assignment form records. It checks for syntax, semantics, and field order of assignment data.
 */
public class AssignmentFormValidation implements Validations<ReviewTask> {

  private static final Logger log = LoggerFactory.getLogger(AssignmentFormValidation.class);

  public AssignmentFormValidation() {}

  /**
   * Validates the syntax of the assignment form record by checking for missing or empty mandatory
   *
   * @param form the input record to be validated
   * @return true if the syntax is valid, false otherwise
   */
  public boolean isValid(AssignmentForm form) {
    if (form.title == null || form.title.trim().isEmpty()) {
      log.warn("Invalid title");
      return false;
    }

    if (form.description == null || form.description.trim().isEmpty()) {
      log.warn("Invalid description");
      return false;
    }

    if (form.dueDate == null || form.dueDate.isBefore(LocalDate.now())) {
      log.warn("Invalid due date");
      return false;
    }

    if (form.startDate == null || form.startDate.isBefore(LocalDate.now())) {
      log.warn("Invalid start date");
      return false;
    }

    if (form.courseCode == null || form.courseCode.trim().isEmpty()) {
      log.warn("Invalid course code");
      return false;
    }

    if (form.questions == null || form.questions.isEmpty()) {
      log.warn("Invalid review questions");
      return false;
    }

    for (AssignmentForm.ReviewQuestionForm q : form.questions) {
      if (q.question == null || q.question.trim().isEmpty()) {
        log.warn("Invalid review question");
        return false;
      }
      if (q.marks < 0 || q.marks > 100) {
        log.warn("Invalid max marks");
        return false;
      }
    }

    return true;
  }

  /**
   * Validates the syntax of the assignment form record by checking for missing or empty mandatory
   * fields.
   *
   * @param record the input record to be validated
   * @return true if the syntax is valid, false otherwise
   */
  @Override
  public boolean validateSyntax(InputRecord record) {
    return false;
  }

  /**
   * Validates the semantics of the assignment form record by checking for existing records and
   * valid group size.
   *
   * @param record the assignment form record to be validated
   * @param repository the repository to check for existing records
   * @return true if the semantics are valid, false otherwise
   */
  @Override
  public boolean validateSemantics(ReviewTask record, Repository<ReviewTask> repository) {
    return false;
  }

  /**
   * Validates the field order of the assignment form record by comparing it with the expected
   * order.
   *
   * @param actualHeaders the actual headers of the CSV file
   * @return true if the field order is correct, false otherwise
   */
  @Override
  public boolean validateFieldOrder(List<String> actualHeaders) {
    return false;
  }
}
