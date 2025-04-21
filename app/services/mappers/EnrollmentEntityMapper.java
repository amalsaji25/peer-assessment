package services.mappers;

import javax.inject.Inject;
import javax.inject.Singleton;
import models.Course;
import models.Enrollment;
import models.User;
import models.dto.Context;
import repository.core.CourseRepository;
import repository.core.UserRepository;
import services.processors.record.InputRecord;

/**
 * EnrollmentEntityMapper is a service class that implements the EntityMapper interface. It provides
 * a method to map input records to Enrollment entities. It uses the UserRepository and
 * CourseRepository to retrieve the student and course associated with the enrollment.
 */
@Singleton
public class EnrollmentEntityMapper implements EntityMapper<Enrollment> {

  private final UserRepository userRepository;
  private final CourseRepository courseRepository;

  @Inject
  public EnrollmentEntityMapper(UserRepository userRepository, CourseRepository courseRepository) {
    this.userRepository = userRepository;
    this.courseRepository = courseRepository;
  }

  /**
   * Maps an input record to an Enrollment entity. It retrieves the student ID, course code, course
   * section, and term from the input record. It also checks if the student file is uploaded and
   * creates a new Enrollment entity with the retrieved values.
   *
   * @param record the input record containing enrollment information
   * @param context the context in which the mapping is performed
   * @return an Enrollment entity mapped from the input record
   */
  @Override
  public Enrollment mapToEntity(InputRecord record, Context context) {
    Long studentId = Long.valueOf(record.get("student_id").trim());
    String courseCode = record.get("course_code").trim();
    String courseSection = record.get("course_section").trim();
    String term = record.get("term").trim();

    User student = userRepository.findById(studentId).orElse(null);
    Course course =
        courseRepository
            .findByCourseCodeAndSectionAndTerm(courseCode, courseSection, term)
            .orElse(null);

    return new Enrollment(student, course, courseSection, term);
  }
}
