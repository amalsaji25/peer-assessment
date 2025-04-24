package services.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import repository.core.EnrollmentRepository;

/**
 * EnrollmentServiceImpl is a service class that implements the EnrollmentService interface. It
 * provides methods to interact with the EnrollmentRepository for managing student enrollments in
 * courses.
 */
public class EnrollmentServiceImpl implements EnrollmentService {

  private final EnrollmentRepository enrollmentRepository;

  @Inject
  public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository) {
    this.enrollmentRepository = enrollmentRepository;
  }

  /**
   * Retrieves the count of students enrolled in a course for a given professor ID, course code,
   * course section, and term.
   *
   * @param userId the ID of the professor
   * @param courseCode the course code
   * @param courseSection the course section
   * @param term the term
   * @return a CompletableFuture containing the count of students enrolled in the course
   */
  @Override
  public CompletableFuture<Integer> getStudentCountByProfessorId(
      Long userId, String courseCode, String courseSection, String term) {
    return enrollmentRepository.getStudentCountByProfessorId(
        userId, courseCode, courseSection, term);
  }

  /**
   * Retrieves the count of students enrolled in a course for a given student ID and course code.
   *
   * @param userId the ID of the student
   * @param courseCode the course code
   * @return a CompletableFuture containing the count of students enrolled in the course
   */
  @Override
  public CompletableFuture<List<Long>> findStudentEnrolledCourseCodes(
      Long userId, String courseCode) {
    if (courseCode == null || courseCode.equalsIgnoreCase("all")) {
      return enrollmentRepository.findCourseCodesByStudentId(userId);
    } else {
      return enrollmentRepository
          .isStudentEnrolledInCourse(userId, courseCode)
          .thenApply(optionalCourseId -> optionalCourseId.map(List::of).orElse(List.of()));
    }
  }

  /**
   * Retrieves the list of courses a student is enrolled in.
   *
   * @param userId the ID of the student
   * @return a CompletableFuture containing a list of courses the student is enrolled in
   */
  @Override
  public CompletableFuture<List<Map<String, String>>> getStudentEnrolledCourse(Long userId) {
    return enrollmentRepository.findEnrolledCoursesForStudentId(userId);
  }
}
