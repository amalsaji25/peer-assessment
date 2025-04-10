package repository.core;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Singleton;

import jakarta.persistence.NoResultException;
import models.Feedback;
import play.db.jpa.JPAApi;

@Singleton
public class FeedbackRepository {
  private final JPAApi jpaApi;
  private final Executor executor = Executors.newFixedThreadPool(5);

  @Inject
  public FeedbackRepository(JPAApi jpaApi) {
    this.jpaApi = jpaApi;
  }

    public Optional<List<Feedback>> findFeedbacksReviewTaskId(Long reviewTaskId) {
        return jpaApi.withTransaction(entityManager -> {
            try {
                List<Feedback> feedback = entityManager
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

    public CompletionStage<List<Feedback>> findFeedbacksReceivedByStudent(Long userId, List<String> courseCodes) {
        return CompletableFuture.supplyAsync(() -> jpaApi.withTransaction(entityManager -> {
            try {
                List<Feedback> feedbacks = entityManager
                        .createQuery(
                                "SELECT f FROM Feedback f WHERE f.reviewTask.reviewee.userId = :userId AND f.reviewTask.assignment.course.courseCode IN :courseCodes",
                                Feedback.class)
                        .setParameter("userId", userId)
                        .setParameter("courseCodes", courseCodes)
                        .getResultList();
                return feedbacks;
            } catch (NoResultException e) {
                return List.of();
            }
        }), executor);
    }

    public void deleteById(Long id) {
        jpaApi.withTransaction(entityManager -> {
            Feedback feedback = entityManager.find(Feedback.class, id);
            if (feedback != null) {
                entityManager.remove(feedback);
            }
        });
    }
}
