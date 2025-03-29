package repository.core;

import models.ReviewTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReviewTaskRepository implements Repository<ReviewTask> {

    private final JPAApi jpaApi;
    private static final Logger log = LoggerFactory.getLogger(ReviewTaskRepository.class);
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    @Inject
    public ReviewTaskRepository(JPAApi jpaApi) {
        this.jpaApi = jpaApi;
    }

    @Override
    public CompletionStage<Map<String, Object>> saveAll(List<ReviewTask> records) {
        return null;
    }

    public Optional<List<ReviewTask>> findByAssignmentId(Long assignmentId) {
        try{
            return jpaApi.withTransaction(entityManager -> {
                List<ReviewTask> reviewTasks = entityManager.createQuery("SELECT rt FROM ReviewTask rt WHERE rt.assignment.assignmentId = :assignmentId", ReviewTask.class)
                        .setParameter("assignmentId", assignmentId)
                        .getResultList();
                if(reviewTasks != null){
                    log.info("Review tasks with assignmentId {} found", assignmentId);
                    return Optional.of(reviewTasks);
                }
                else{
                    log.warn("Review tasks with assignmentId {} not found", assignmentId);
                    return Optional.empty();
                }
            });
        }catch (Exception e) {
            log.error("failed to find review tasks with assignmentId {} - with exception: {}", assignmentId, e.getMessage());
            return Optional.empty();
        }
    }


    public Boolean validateIfReviewerAndRevieweeIsEnrolledInCourse(Long reviewerUserId, Long revieweeUserId, String courseCode) {

        return jpaApi.withTransaction(entityManager -> {
            String queryString = "SELECT COUNT(e) FROM Enrollment e WHERE e.student.userId IN (:reviewerUserId, :revieweeUserId) AND e.course.courseCode = :courseCode";
            Long count = entityManager.createQuery(queryString, Long.class)
                    .setParameter("reviewerUserId", reviewerUserId)
                    .setParameter("revieweeUserId", revieweeUserId)
                    .setParameter("courseCode", courseCode)
                    .getSingleResult();
            return count > 0;
        });
    }
}
