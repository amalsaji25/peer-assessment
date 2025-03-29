package services.mappers;

import models.Course;
import models.User;
import org.apache.commons.csv.CSVRecord;
import repository.core.UserRepository;

import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public class CourseEntityMapper implements EntityMapper<Course> {

    private final UserRepository userRepository;

    @Inject
    public CourseEntityMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Course mapToEntity(CSVRecord record) {
        String courseCode = record.get("course_code").trim();
        String courseName = record.get("course_name").trim();
        Long professorId = Long.valueOf(record.get("professor_id").trim());

        User professor = userRepository.findById(professorId).orElse(null);

        return new Course(courseCode, courseName, professor);
    }
}
