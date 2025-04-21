package repository.core;

import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.Assignment;
import models.ReviewTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.jpa.JPAApi;

/**
 * AssignmentRepository is a singleton class responsible for managing database operations related to
 * the Assignment entity. It provides methods to save, update, delete, and retrieve assignments,
 * including their associated feedback questions and review tasks.
 */
@Singleton
public class AssignmentRepository {

  private static final Logger log = LoggerFactory.getLogger(AssignmentRepository.class);
  private final JPAApi jpaApi;
  private final ExecutorService executorService = Executors.newFixedThreadPool(5);

  @Inject
  public AssignmentRepository(JPAApi jpaApi) {
    this.jpaApi = jpaApi;
  }

  /**
   * Asynchronous method to find the count of assignments for a given professor ID and optional
   * course details (course code, course section, term).
   *
   * @param userId The ID of the professor.
   * @param courseCode The course code (optional).
   * @param courseSection The course section (optional).
   * @param term The term (optional).
   * @return A CompletableFuture containing the count of assignments.
   */
  public CompletableFuture<Integer> findAssignmentCountByProfessorId(
      Long userId, String courseCode, String courseSection, String term) {
    return CompletableFuture.supplyAsync(
        () ->
            jpaApi.withTransaction(
                entityManager -> {
                  String queryString;
                  TypedQuery<Long> query;

                  if (courseCode != null && courseSection != null && term != null) {
                    queryString =
                        "SELECT COUNT(a) FROM Assignment a "
                            + "WHERE a.course.professor.id = :userId AND a.course.courseCode = :courseCode AND a.course.courseSection = :courseSection AND a.course.term = :term";
                    query =
                        entityManager
                            .createQuery(queryString, Long.class)
                            .setParameter("userId", userId)
                            .setParameter("courseCode", courseCode)
                            .setParameter("courseSection", courseSection)
                            .setParameter("term", term);
                  } else {
                    queryString =
                        "SELECT COUNT(a) FROM Assignment a "
                            + "WHERE a.course.professor.id = :userId ";

                    query =
                        entityManager
                            .createQuery(queryString, Long.class)
                            .setParameter("userId", userId);
                  }
                  return query.getSingleResult().intValue();
                }),
        executorService);
  }

  /**
   * Asynchronous method to save an assignment to the database.
   *
   * @param assignment The assignment to be saved.
   * @return A CompletableFuture containing the ID of the saved assignment.
   */
  public CompletableFuture<Long> save(Assignment assignment) {
    return CompletableFuture.supplyAsync(
        () ->
            jpaApi.withTransaction(
                entityManager -> {
                  entityManager.persist(assignment);
                  return assignment.getAssignmentId();
                }),
        executorService);
  }

  public void update(Assignment assignment) {
    jpaApi.withTransaction(
        entityManager -> {
          entityManager.merge(assignment);
        });
  }

  /**
   * Asynchronous method to find an assignment by its ID, including its feedback questions.
   *
   * @param assignmentId The ID of the assignment to be found.
   * @return An Optional containing the found assignment, or empty if not found.
   */
  public Optional<Assignment> findByIdWithFeedbackQuestions(Long assignmentId) {
    try {
      return jpaApi.withTransaction(
          entityManager -> {
            TypedQuery<Assignment> query =
                entityManager.createQuery(
                    "SELECT a FROM Assignment a LEFT JOIN FETCH a.feedbackQuestions WHERE a.assignmentId = :assignmentId",
                    Assignment.class);
            query.setParameter("assignmentId", assignmentId);

            Assignment result = query.getSingleResult();
            for (ReviewTask task : result.getReviewTasks()) {
              task.getFeedbacks().size(); // Triggers loading
            }
            log.info("Assignment {} with feedbackQuestions fetched from repo", assignmentId);
            return Optional.ofNullable(result);
          });
    } catch (Exception e) {
      log.error(
          "findByIdWithFeedbackQuestions failed for assignment {} with exception: {}",
          assignmentId,
          e.getMessage());
      return Optional.empty();
    }
  }

  /**
   * Asynchronous method to find all assignments for a given course code, course section, and term.
   *
   * @param courseCode The course code.
   * @param courseSection The course section.
   * @param term The term.
   * @return A CompletableFuture containing a list of assignments.
   */
  public CompletableFuture<List<Map<String, Object>>> findAssignmentsByCourse(
      String courseCode, String courseSection, String term) {
    try {
      return CompletableFuture.supplyAsync(
          () ->
              jpaApi.withTransaction(
                  entityManager -> {
                    TypedQuery<Object[]> query =
                        entityManager.createQuery(
                            "SELECT a.assignmentId, a.title FROM Assignment a WHERE a.course.courseCode = :courseId AND a.course.courseSection = :courseSection AND a.course.term = :term",
                            Object[].class);
                    query
                        .setParameter("courseId", courseCode)
                        .setParameter("courseSection", courseSection)
                        .setParameter("term", term);
                    return query.getResultList().stream()
                        .map(
                            result ->
                                Map.of(
                                    "assignmentId", result[0],
                                    "title", result[1]))
                        .toList();
                  }),
          executorService);
    } catch (Exception e) {
      log.error(
          "findAssignmentsByCourse failed for course {} with exception: {}",
          courseCode,
          e.getMessage());
      return CompletableFuture.completedFuture(List.of());
    }
  }

  /**
   * Asynchronous method to delete an assignment by its ID, including its associated feedbacks,
   * review tasks, and feedback questions.
   *
   * @param assignmentId The ID of the assignment to be deleted.
   * @return A CompletableFuture containing a boolean indicating success or failure.
   */
  public CompletableFuture<Boolean> deleteAssignmentById(Long assignmentId) {
    return CompletableFuture.supplyAsync(
        () ->
            jpaApi.withTransaction(
                entityManager -> {
                  try {
                    // Step 1: Delete feedbacks linked to this assignment through review tasks
                    entityManager
                        .createQuery(
                            "DELETE FROM Feedback f WHERE f.reviewTask.assignment.assignmentId = :assignmentId")
                        .setParameter("assignmentId", assignmentId)
                        .executeUpdate();

                    // Step 2: Delete feedback questions
                    entityManager
                        .createQuery(
                            "DELETE FROM FeedbackQuestion fq WHERE fq.assignment.assignmentId = :assignmentId")
                        .setParameter("assignmentId", assignmentId)
                        .executeUpdate();

                    // Step 3: Delete review tasks
                    entityManager
                        .createQuery(
                            "DELETE FROM ReviewTask rt WHERE rt.assignment.assignmentId = :assignmentId")
                        .setParameter("assignmentId", assignmentId)
                        .executeUpdate();

                    // Step 4: Delete the assignment itself
                    int deletedCount =
                        entityManager
                            .createQuery(
                                "DELETE FROM Assignment a WHERE a.assignmentId = :assignmentId")
                            .setParameter("assignmentId", assignmentId)
                            .executeUpdate();

                    if (deletedCount > 0) {
                      log.info(
                          "Assignment {} and its dependencies deleted successfully", assignmentId);
                      return true;
                    } else {
                      log.warn("Assignment {} not found for deletion", assignmentId);
                      return false;
                    }

                  } catch (Exception e) {
                    log.error("Error deleting assignment {}: {}", assignmentId, e.getMessage(), e);
                    return false;
                  }
                }),
        executorService);
  }

  /**
   * Asynchronous method to find an assignment by its ID.
   *
   * @param assignmentId The ID of the assignment to be found.
   * @return An Optional containing the found assignment, or empty if not found.
   */
  public Optional<Assignment> findById(Long assignmentId) {
    try {
      return jpaApi.withTransaction(
          entityManager -> {
            return entityManager
                .createQuery(
                    "SELECT a FROM Assignment a WHERE a.assignmentId = :assignmentId",
                    Assignment.class)
                .setParameter("assignmentId", assignmentId)
                .getResultStream()
                .findFirst()
                .map(
                    a -> {
                      a.getReviewTasks().forEach(rt -> rt.getFeedbacks().size());
                      a.getFeedbackQuestions().size();
                      return a;
                    });
          });
    } catch (Exception e) {
      log.error(
          "findById failed for assignment {} with exception: {}", assignmentId, e.getMessage());
      return Optional.empty();
    }
  }

  /**
   * Asynchronous method to find the count of assignments for a list of course codes.
   *
   * @param courseCodes The list of course codes.
   * @return A CompletableFuture containing the count of assignments.
   */
  public CompletableFuture<Integer> findAssignmentCountByCourseCodes(List<String> courseCodes) {
    return CompletableFuture.supplyAsync(
        () ->
            jpaApi.withTransaction(
                entityManager -> {
                  TypedQuery<Long> query =
                      entityManager.createQuery(
                          "SELECT COUNT(a) FROM Assignment a WHERE a.course.courseCode IN :courseCodes",
                          Long.class);
                  query.setParameter("courseCodes", courseCodes);
                  return query.getSingleResult().intValue();
                }),
        executorService);
  }
}
