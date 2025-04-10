package services.validations;

import java.util.List;
import repository.core.Repository;
import services.processors.record.InputRecord;

/**
 * Validations is an interface that defines methods for validating input records. It includes
 * methods for syntax validation, semantic validation, and field order validation.
 *
 * @param <T> the type of entity to be validated
 */
public interface Validations<T> {
  boolean validateSyntax(InputRecord record); // Check for syntax and missing fields

  boolean validateSemantics(T record, Repository<T> repository); // Validate business requirements

  boolean validateFieldOrder(List<String> actualHeaders);
}
