package services.validations;

import models.Enrollment;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.core.Repository;
import services.processors.record.InputRecord;

import javax.inject.Singleton;
import java.util.List;

@Singleton
public class EnrollmentValidation implements Validations<Enrollment> {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentValidation.class);
    private static final List<String> MANDATORY_FIELDS = List.of("student_id", "course_code","course_section", "term");
    private static final List<String> EXPECTED_FIELDS_ORDER = List.of("student_id", "course_code", "course_section", "term");

    @Override
    public boolean validateSyntax(InputRecord record) {
        boolean isValidSyntax = MANDATORY_FIELDS.stream().allMatch(field -> record.isMapped(field) && !record.get(field).isEmpty());

        if (!isValidSyntax) {
            log.warn("Skipping invalid enrollment record due to missing mandatory fields: {}", record);
        }
        return isValidSyntax;
    }

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

    @Override
    public boolean validateFieldOrder(List<String> actualHeaders) {
        boolean isCorrectOrder = actualHeaders.equals(EXPECTED_FIELDS_ORDER);
        if (!isCorrectOrder) {
            log.warn("File has incorrect field order. Expected order is {}, Found order is {}", EXPECTED_FIELDS_ORDER, actualHeaders);
        }
        return isCorrectOrder;
    }
}