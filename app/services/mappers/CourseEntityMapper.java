package services.mappers;

import models.Courses;
import models.Users;
import org.apache.commons.csv.CSVRecord;
import repository.UserRepository;

import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public class CourseEntityMapper implements EntityMapper<Courses> {

    private final UserRepository userRepository;

    @Inject
    public CourseEntityMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Courses mapToEntity(CSVRecord record) {
        String courseCode = record.get("course_code").trim();
        String courseName = record.get("course_name").trim();
        String professorId = record.get("professor_id").trim();

        Users professor = userRepository.findById(professorId).orElse(null);

        return new Courses(courseCode, courseName, professor);
    }
}
