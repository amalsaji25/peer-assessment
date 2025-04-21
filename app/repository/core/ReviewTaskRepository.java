package repository.core;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import models.Assignment;
import models.Course;
import models.Feedback;
import models.ReviewTask;
import models.dto.Context;
import models.dto.FeedbackDTO;
import models.dto.ReviewTaskDTO;
import models.enums.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.jpa.JPAApi;

/**
 * ReviewTaskRepository is a singleton class that handles the persistence of ReviewTask entities in
 * the database. It provides methods to save review tasks, find review tasks by assignment ID, and
 * update feedback for review tasks.
 */
public class ReviewTaskRepository implements Repository<ReviewTask> {

    private static final Logger log = LoggerFactory.getLogger(ReviewTaskRepository.class);
    private final JPAApi jpaApi;
    private final ExecutorService executor = Executors.newFixedThreadPool(5);


    @Inject
    public ReviewTaskRepository(JPAApi jpaApi) {
        this.jpaApi = jpaApi;
    }

    /**
     * Saves a list of review tasks to the database.
     *
     * @param reviewTasks the list of review tasks to be saved
     * @param context     the context containing additional information
     * @return a CompletionStage containing a map with success and failure counts
     */
    @Override
    public CompletionStage<Map<String, Object>> saveAll(List<ReviewTask> reviewTasks, Context context) {
        String courseCode = context.getCourseCode();
        String courseSection = context.getCourseSection();
        String term = context.getTerm();

        return CompletableFuture.supplyAsync(() -> {
            return jpaApi.withTransaction(entityManager -> {
                Course course = entityManager.createQuery(
                                "SELECT c FROM Course c WHERE c.courseCode = :courseCode AND c.courseSection = :section AND c.term = :term", Course.class)
                        .setParameter("courseCode", courseCode)
                        .setParameter("section", courseSection)
                        .setParameter("term", term)
                        .getResultStream()
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Course not found with the provided details."));

                List<Assignment> assignments = entityManager.createQuery(
                                "SELECT a FROM Assignment a WHERE a.course = :course AND a.peerAssigned = false", Assignment.class)
                        .setParameter("course", course)
                        .getResultList();

                if (assignments.isEmpty()) {
                    throw new IllegalArgumentException("All assignments for this course have already been peer assigned.");
                }

                int successCount = 0;
                List<Long> failedAssignmentIds = new ArrayList<>();

                for (Assignment assignment : assignments) {
                    try {
                        // Link assignment to each review task
                        reviewTasks.forEach(reviewTask -> {
                            reviewTask.setAssignment(assignment);
                            List<Feedback> feedbacks = new ArrayList<>();
                            if(!reviewTask.isReviewTaskForProfessor()){
                                feedbacks = assignment.getFeedbackQuestions().stream()
                                        .filter(feedbackQuestion -> !feedbackQuestion.getQuestionText().equalsIgnoreCase("Private Comment for Professor"))
                                        .map(question -> {
                                    Feedback feedback = new Feedback();
                                    feedback.setQuestion(question);
                                    feedback.setReviewTask(reviewTask);
                                    feedback.setScore(0);
                                    feedback.setFeedbackText("");
                                    return feedback;
                                }).toList();
                            }
                            else{
                                feedbacks = assignment.getFeedbackQuestions().stream()
                                        .filter(feedbackQuestion -> feedbackQuestion.getQuestionText().equalsIgnoreCase("Private Comment for Professor"))
                                        .map(question -> {
                                            Feedback feedback = new Feedback();
                                            feedback.setQuestion(question);
                                            feedback.setReviewTask(reviewTask);
                                            feedback.setScore(0);
                                            feedback.setFeedbackText("");
                                            return feedback;
                                        }).toList();
                            }
                            reviewTask.setFeedbacks(feedbacks);
                        });

                        // Attach to assignment
                        assignment.getReviewTasks().clear();
                        assignment.getReviewTasks().addAll(reviewTasks);
                        assignment.setPeerAssigned(true);

                        log.info("Saving assignment {} with {} review tasks", assignment.getAssignmentId(), assignment.getReviewTasks().size());
                        entityManager.merge(assignment); // cascade persists review tasks and feedbacks

                        successCount += reviewTasks.size();
                        log.info("Attached {} review tasks to assignment {}", reviewTasks.size(), assignment.getAssignmentId());
                    } catch (Exception e) {
                        log.error("Failed to attach review tasks to assignment {}", assignment.getAssignmentId(), e);
                        failedAssignmentIds.add(assignment.getAssignmentId());
                    }
                }

                Map<String, Object> result = new HashMap<>();
                result.put("successCount", successCount);
                result.put("failedAssignments", failedAssignmentIds);
                result.put("totalAssignmentsProcessed", assignments.size());

                return result;
            });
        }, executor);
    }

    /**
     * Finds review tasks by assignment ID.
     *
     * @param assignmentId the ID of the assignment
     * @return an Optional containing a list of ReviewTask objects if found, or an empty Optional if not found
     */
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

    /**
     * Finds review tasks by assignment ID and status.
     * @param userId the ID of the user
     * @param status the status of the review task
     * @return a CompletableFuture containing the count of review tasks
     */
    public CompletableFuture<Integer> findReviewCountByStudentIdAndStatus(Long userId, Status status) {
        return CompletableFuture.supplyAsync(() -> jpaApi.withTransaction(entityManager -> {
            String queryString = "SELECT COUNT(rt) FROM ReviewTask rt WHERE rt.reviewer.userId = :userId AND rt.status = :status";
            Long count = entityManager.createQuery(queryString, Long.class)
                    .setParameter("userId", userId)
                    .setParameter("status", status)
                    .getSingleResult();
            return count.intValue();
        }), executor);
    }

    /**
     * Finds review tasks by student ID and status for a specific course.
     *
     * @param userId the ID of the student
     * @param courseCode the course code
     * @param status the status of the review task
     * @return a CompletableFuture containing the count of review tasks
     */
    public CompletableFuture<Integer> findReviewCountByStudentIdAndStatusForCourse(Long userId, String courseCode, Status status) {
        return CompletableFuture.supplyAsync(() -> jpaApi.withTransaction(entityManager -> {
            String queryString = "SELECT COUNT(rt) FROM ReviewTask rt WHERE rt.reviewer.userId = :userId AND rt.status = :status AND rt.assignment.course.courseCode = :courseCode";
            Long count = entityManager.createQuery(queryString, Long.class)
                    .setParameter("userId", userId)
                    .setParameter("status", status)
                    .setParameter("courseCode", courseCode)
                    .getSingleResult();
            return count.intValue();
        }), executor);
    }

    /**
     * Saves feedback for review tasks.
     *
     * @param reviewTaskDTO the DTO containing review task and feedback information
     */
    public void saveReviewTaskFeedback(ReviewTaskDTO reviewTaskDTO) {
        jpaApi.withTransaction(entityManager -> {

            // Update feedback entries
            for (FeedbackDTO feedbackDTO : reviewTaskDTO.getFeedbacks()) {
                Feedback feedback = entityManager.find(Feedback.class, feedbackDTO.getFeedbackId());
                if (feedback != null) {
                    feedback.setFeedbackText(feedbackDTO.getFeedbackText());
                    feedback.setScore(feedbackDTO.getObtainedScore());
                    entityManager.merge(feedback);
                }

                // Update review task status
                ReviewTask reviewTask = entityManager.find(ReviewTask.class, reviewTaskDTO.getReviewTaskId());
                if (reviewTask != null) {
                    reviewTask.setStatus(reviewTaskDTO.getReviewStatus());
                    entityManager.merge(reviewTask);
                }
            }
        });
    }
}
