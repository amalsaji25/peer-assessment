package services.mappers;

import models.Courses;
import models.Enrollments;
import models.Users;
import org.apache.commons.csv.CSVRecord;
import repository.CourseRepository;
import repository.UserRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EnrollmentEntityMapper implements EntityMapper<Enrollments>{

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Inject
    public EnrollmentEntityMapper(UserRepository userRepository, CourseRepository courseRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public Enrollments mapToEntity(CSVRecord record) {
        Long studentId = Long.valueOf(record.get("student_id").trim());
        String courseId = record.get("course_id").trim();

        Users student = userRepository.findById(studentId).orElse(null);
        Courses course = courseRepository.findByCourseCode(courseId).orElse(null);

    return new Enrollments(student, course);
  }
}
