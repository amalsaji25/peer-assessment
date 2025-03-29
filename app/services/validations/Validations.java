package services.validations;

import org.apache.commons.csv.CSVRecord;
import repository.core.Repository;

import java.util.List;

public interface Validations<T> {
    boolean validateSyntax(CSVRecord record); // Check for syntax and missing fields
    boolean validateSemantics(T record, Repository<T> repository); // Validate business requirements
    boolean validateFieldOrder(List<String> actualHeaders);
}
