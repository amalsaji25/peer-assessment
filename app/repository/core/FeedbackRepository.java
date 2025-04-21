package repository.core;

import jakarta.persistence.NoResultException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.Feedback;
import play.db.jpa.JPAApi;

/**
 * FeedbackRepository is a singleton class that handles the persistence of Feedback entities in the
 * database. It provides methods to find feedback by review task ID, find feedback received by a
 * student, and delete feedback by ID.
 */
@Singleton
public class FeedbackRepository {
  private final JPAApi jpaApi;
  private final Executor executor = Executors.newFixedThreadPool(5);

  @Inject
  public FeedbackRepository(JPAApi jpaApi) {
    this.jpaApi = jpaApi;
  }

  /**
   * Finds feedback by review task ID.
   *
   * @param reviewTaskId the ID of the review task
   * @return an Optional containing a list of Feedback objects if found, or an empty Optional if not
   *     found
   */
  public Optional<List<Feedback>> findFeedbacksReviewTaskId(Long reviewTaskId) {
    return jpaApi.withTransaction(
        entityManager -> {
          try {
            List<Feedback> feedback =
                entityManager
                    .createQuery(
                        "SELECT f FROM Feedback f WHERE f.reviewTask.id = :reviewTaskId",
                        Feedback.class)
                    .setParameter("reviewTaskId", reviewTaskId)
                    .getResultList();
            return Optional.of(feedback);
          } catch (NoResultException e) {
            return Optional.empty();
          }
        });
  }

  /**
   * Finds feedback received by a student for a list of course codes.
   *
   * @param userId the ID of the student
   * @param courseIds a list of course codes
   * @return a CompletionStage containing a list of Feedback objects
   */
  public CompletionStage<List<Feedback>> findFeedbacksReceivedByStudent(
      Long userId, List<Long> courseIds) {
    return CompletableFuture.supplyAsync(
        () ->
            jpaApi.withTransaction(
                entityManager -> {
                  try {
                    return entityManager
                        .createQuery(
                            "SELECT f FROM Feedback f WHERE f.reviewTask.reviewee.userId = :userId AND f.reviewTask.assignment.course.courseId IN :courseIds",
                            Feedback.class)
                        .setParameter("userId", userId)
                        .setParameter("courseIds", courseIds)
                        .getResultList();
                  } catch (NoResultException e) {
                    return List.of();
                  }
                }),
        executor);
  }

  /**
   * Deletes feedback by ID.
   *
   * @param id the ID of the feedback
   */
  public void deleteById(Long id) {
    jpaApi.withTransaction(
        entityManager -> {
          Feedback feedback = entityManager.find(Feedback.class, id);
          if (feedback != null) {
            entityManager.remove(feedback);
          }
        });
  }
}
