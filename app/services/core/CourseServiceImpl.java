package services.core;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import models.Course;
import repository.core.CourseRepository;

/**
 * CourseServiceImpl is a service class that implements the CourseService interface. It provides
 * methods to interact with the CourseRepository for managing courses in the system.
 */
public class CourseServiceImpl implements CourseService {

  private final CourseRepository courseRepository;

  @Inject
  public CourseServiceImpl(CourseRepository courseRepository) {
    this.courseRepository = courseRepository;
  }

  /**
   * Retrieves the count of active courses for a given professor ID, course code, course section,
   * and term.
   *
   * @param userId the ID of the professor
   * @param courseCode the course code
   * @param courseSection the course section
   * @param term the term
   * @return a CompletableFuture containing the count of active courses
   */
  @Override
  public CompletableFuture<Integer> getActiveCoursesByProfessorId(
      Long userId, String courseCode, String courseSection, String term) {
    return courseRepository.findActiveCoursesByProfessorId(userId, courseCode, courseSection, term);
  }

  /**
   * Retrieves the count of active courses for a given student ID and course code.
   *
   * @param userId the ID of the student
   * @param term the term
   * @return a CompletableFuture containing the count of active courses
   */
  @Override
  public CompletableFuture<List<Map<String, String>>> getAllCourses(Long userId, String term) {
    return courseRepository.findAllCourses(userId, term);
  }

  /**
   * Retrieves the count of active courses for a given student ID and course code.
   *
   * @param userId the ID of the student
   * @return a CompletableFuture containing the count of active courses
   */
  @Override
  public CompletableFuture<List<String>> getAllTerms(Long userId) {
    return courseRepository
        .findAllTerms(userId)
        .thenApply(
            existingTerms -> {
              Set<String> terms = new LinkedHashSet<>(existingTerms);
              List<String> updatedTerms = new ArrayList<>(existingTerms);

              LocalDateTime now = LocalDateTime.now();
              int currentYear = now.getYear();
              int currentMonth = now.getMonthValue();

              int yearToUse =
                  (currentMonth == Month.AUGUST.getValue()) ? currentYear + 1 : currentYear;

              List<String> possibleTerms =
                  Arrays.asList(
                      "Winter " + yearToUse,
                      "Summer 1 " + yearToUse,
                      "Summer 2 " + yearToUse,
                      "Summer " + yearToUse,
                      "Fall " + yearToUse);

              for (String term : possibleTerms) {
                if (!terms.contains(term)) {
                  updatedTerms.add(term);
                }
              }
              return updatedTerms;
            });
  }

  /**
   * Assigns a course to a professor.
   *
   * @param courseData the course data in the format "courseCode:::courseSection:::term"
   * @return a CompletionStage containing true if the course was assigned successfully, false
   *     otherwise
   */
  @Override
  public CompletionStage<Boolean> unassignCourse(String courseData) {

    String courseCode = courseData.split(":::")[0].trim();
    String courseSection = courseData.split(":::")[1].trim();
    String term = courseData.split(":::")[2].trim();
    Optional<Course> course =
        courseRepository.findByCourseCodeAndSectionAndTerm(courseCode, courseSection, term);
    if (course.isPresent()) {
      Long courseId = course.get().getCourseId();
      courseRepository.unassignCourse(courseId);
      return CompletableFuture.completedFuture(true);
    } else {
      return CompletableFuture.completedFuture(false);
    }
  }
}
