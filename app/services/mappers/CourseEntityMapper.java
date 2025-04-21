package services.mappers;

import javax.inject.Inject;
import javax.inject.Singleton;
import models.Course;
import models.User;
import models.dto.Context;
import repository.core.UserRepository;
import services.processors.record.InputRecord;

/**
 * CourseEntityMapper is a service class that implements the EntityMapper interface. It provides a
 * method to map input records to Course entities. It uses the UserRepository to retrieve the
 * professor associated with the course.
 */
@Singleton
public class CourseEntityMapper implements EntityMapper<Course> {

  private final UserRepository userRepository;

  @Inject
  public CourseEntityMapper(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Maps an input record to a Course entity. It retrieves the course code, course name, professor
   * ID, course section, and term from the input record. It also checks if the student file is
   * uploaded and creates a new Course entity with the retrieved values.
   *
   * @param record the input record containing course information
   * @param context the context in which the mapping is performed
   * @return a Course entity mapped from the input record
   */
  @Override
  public Course mapToEntity(InputRecord record, Context context) {
    String courseCode = record.get("course_code").trim();
    String courseName = record.get("course_name").trim();
    Long professorId = Long.valueOf(record.get("professor_id").trim());
    String courseSection = record.get("course_section").trim();
    Boolean isStudentFileUploaded = false;

    User professor = userRepository.findById(professorId).orElse(null);
    String term = record.get("term").trim();

    return new Course(
        courseCode, courseName, professor, term, courseSection, isStudentFileUploaded);
  }
}
