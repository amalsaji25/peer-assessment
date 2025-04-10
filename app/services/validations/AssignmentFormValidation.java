package services.validations;

import forms.AssignmentForm;
import models.ReviewTask;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.core.Repository;
import services.processors.record.InputRecord;

import java.time.LocalDate;
import java.util.List;

public class AssignmentFormValidation implements Validations<ReviewTask> {

  private static final Logger log = LoggerFactory.getLogger(AssignmentFormValidation.class);

  public AssignmentFormValidation() {}

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

  @Override
  public boolean validateSyntax(InputRecord record) {
    return false;
  }

  @Override
  public boolean validateSemantics(ReviewTask record, Repository<ReviewTask> repository) {
    return false;
  }

  @Override
  public boolean validateFieldOrder(List<String> actualHeaders) {
    return false;
  }
}
