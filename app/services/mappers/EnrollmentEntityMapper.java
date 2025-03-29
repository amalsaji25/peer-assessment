package services.mappers;

import models.Course;
import models.Enrollment;
import models.User;
import org.apache.commons.csv.CSVRecord;
import repository.core.CourseRepository;
import repository.core.UserRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EnrollmentEntityMapper implements EntityMapper<Enrollment>{

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Inject
    public EnrollmentEntityMapper(UserRepository userRepository, CourseRepository courseRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public Enrollment mapToEntity(CSVRecord record) {
        Long studentId = Long.valueOf(record.get("student_id").trim());
        String courseCode = record.get("course_code").trim();

        User student = userRepository.findById(studentId).orElse(null);
        Course course = courseRepository.findByCourseCode(courseCode).orElse(null);

    return new Enrollment(student, course);
  }
}
