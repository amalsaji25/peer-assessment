package services.validations;

import models.Course;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.core.CourseRepository;
import repository.core.Repository;

import javax.inject.Singleton;
import java.util.List;

@Singleton
public class CourseValidation implements Validations<Course> {

    private static final Logger log = LoggerFactory.getLogger(CourseValidation.class);
    private static final List<String> MANDATORY_FIELDS = List.of("course_code", "course_name", "professor_id");
    private static final List<String> EXPECTED_FIELDS_ORDER = List.of("course_code", "course_name", "professor_id");

    @Override
    public boolean validateSyntax(CSVRecord record) {
        boolean isValidSyntax = MANDATORY_FIELDS.stream().allMatch(field -> record.isMapped(field) && !record.get(field).isEmpty());

        if (!isValidSyntax) {
            log.warn("Skipping invalid course record due to missing mandatory fields: {}", record);
        }
        return isValidSyntax;
    }

    @Override
    public boolean validateSemantics(Course record, Repository<Course> repository) {
        CourseRepository courseRepository = (CourseRepository) repository;

        // Check if the course already exists
        if (courseRepository.findByCourseCode(record.getCourseCode()).isPresent()) {
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

    @Override
    public boolean validateFieldOrder(List<String> actualHeaders) {
        boolean isCorrectOrder = actualHeaders.equals(EXPECTED_FIELDS_ORDER);
        if (!isCorrectOrder) {
            log.warn("File has incorrect field order. Expected order is {}, Found order is {}", EXPECTED_FIELDS_ORDER, actualHeaders);
        }
        return isCorrectOrder;
    }
}