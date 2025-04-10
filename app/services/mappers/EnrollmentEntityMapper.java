package services.mappers;

import models.Course;
import models.Enrollment;
import models.User;
import models.dto.Context;
import repository.core.CourseRepository;
import repository.core.UserRepository;
import services.processors.record.InputRecord;

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
    public Enrollment mapToEntity(InputRecord record, Context context) {
        Long studentId = Long.valueOf(record.get("student_id").trim());
        String courseCode = record.get("course_code").trim();
        String courseSection = record.get("course_section").trim();
        String term = record.get("term").trim();

        User student = userRepository.findById(studentId).orElse(null);
        Course course = courseRepository.findByCourseCodeAndSectionAndTerm(courseCode, courseSection, term).orElse(null);

    return new Enrollment(student, course, courseSection, term);
  }
}
