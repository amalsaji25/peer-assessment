package repository.core;

import models.FeedbackQuestion;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Singleton
public class FeedbackQuestionRepository {

    private final JPAApi jpaApi;
    private final Executor executor = Executors.newFixedThreadPool(5);

    @Inject
    public FeedbackQuestionRepository(JPAApi jpaApi) {
        this.jpaApi = jpaApi;
    }


    public CompletableFuture<Void> save(FeedbackQuestion feedbackQuestion) {
        return CompletableFuture.runAsync(() -> jpaApi.withTransaction(entityManager -> {
            entityManager.persist(feedbackQuestion);
        }), executor);
    }
}
