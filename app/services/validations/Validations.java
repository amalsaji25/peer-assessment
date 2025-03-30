package services.validations;

import org.apache.commons.csv.CSVRecord;
import repository.core.Repository;
import services.processors.record.InputRecord;

import java.util.List;

public interface Validations<T> {
    boolean validateSyntax(InputRecord record); // Check for syntax and missing fields
    boolean validateSemantics(T record, Repository<T> repository); // Validate business requirements
    boolean validateFieldOrder(List<String> actualHeaders);
}
