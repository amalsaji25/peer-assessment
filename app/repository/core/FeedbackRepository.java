package repository.core;

import java.util.List;
import java.util.Optional;
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
}
