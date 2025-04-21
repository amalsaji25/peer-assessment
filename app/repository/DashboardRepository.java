package repository;

import jakarta.persistence.TypedQuery;
import models.Assignment;
import models.ReviewTask;
import models.dto.FeedbackDTO;
import models.dto.PeerReviewSummaryDTO;
import models.dto.ReviewTaskDTO;
import models.enums.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * DashboardRepository is a singleton class that handles the retrieval of assignment and peer review
 * data for professors and students. It provides methods to get peer review progress, assignment
 * summaries, and pending peer reviews.
 */
@Singleton
public class DashboardRepository {
    private final JPAApi jpaApi;
    private static final Logger log = LoggerFactory.getLogger(DashboardRepository.class);
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    @Inject
    public DashboardRepository(JPAApi jpaApi) {
        this.jpaApi = jpaApi;
    }


    /**
     * Retrieves the peer review progress for a professor based on the provided filters.
     *
     * @param userId          The ID of the professor.
     * @param filterByCourseCode The course code to filter by (optional).
     * @param courseSection   The course section to filter by (optional).
     * @param term           The term to filter by (optional).
     * @return A CompletableFuture containing a list of PeerReviewSummaryDTO objects.
     */
    public CompletableFuture<List<PeerReviewSummaryDTO>> getPeerReviewProgressForProfessor(Long userId, String filterByCourseCode, String courseSection, String term) {
        return CompletableFuture.supplyAsync(()->jpaApi.withTransaction(entityManger -> {

            List<PeerReviewSummaryDTO> result = new ArrayList<>();

            String queryString;
            TypedQuery<Assignment> query;

            if(filterByCourseCode != null && courseSection != null && term != null){
                queryString = "SELECT a FROM Assignment a WHERE a.course.courseCode = :courseCode AND a.course.professor.userId = :userId AND a.course.courseSection = :courseSection AND a.course.term = :term ";
                query = entityManger.createQuery(queryString, Assignment.class)
                        .setParameter("courseCode", filterByCourseCode)
                        .setParameter("userId", userId)
                        .setParameter("courseSection", courseSection)
                        .setParameter("term", term);
            }
            else{
                queryString = "SELECT a FROM Assignment a WHERE a.course.professor.userId = :userId ";
                query = entityManger.createQuery(queryString, Assignment.class)
                        .setParameter("userId", userId);
            }

            List<Assignment> assignments = query.setParameter("userId", userId).getResultList();

            for (Assignment assignment : assignments) {
                Long assignmentId = assignment.getAssignmentId();
                String courseCode = assignment.getCourse().getCourseCode();
                Long courseId = assignment.getCourse().getCourseId();

                // Fetch review tasks
                List<ReviewTask> reviewTasks = entityManger.createQuery(
                                "SELECT r FROM ReviewTask r WHERE r.assignment.assignmentId = :assignmentId", ReviewTask.class)
                        .setParameter("assignmentId", assignmentId)
                        .getResultList();

                int totalReviews = reviewTasks.size();

                // Task-based completed reviews
                int completedReviewTasks = (int) reviewTasks.stream()
                        .filter(rt -> rt.getStatus() == Status.COMPLETED)
                        .count();

                // Distinct reviewers (members)
                Set<Long> distinctReviewers = reviewTasks.stream()
                        .map(rt -> rt.getReviewer().getUserId())
                        .collect(Collectors.toSet());

                int totalMembers = distinctReviewers.size();

                // Members who completed all their assigned reviews
                int completedMembers = (int) distinctReviewers.stream()
                        .filter(reviewerId -> reviewTasks.stream()
                                .filter(rt -> rt.getReviewer().getUserId().equals(reviewerId))
                                .allMatch(rt -> rt.getStatus() == Status.COMPLETED))
                        .count();

                // Member-based progress
                int progress = totalMembers == 0 ? 0 : Math.round((completedMembers * 100f) / totalMembers);

                // Total students enrolled
                int studentCount = entityManger.createQuery(
                                "SELECT COUNT(e) FROM Enrollment e WHERE e.course.courseId = :courseId", Long.class)
                        .setParameter("courseId", courseId)
                        .getSingleResult()
                        .intValue();

                String courseInfo = courseCode + " (" + assignment.getCourse().getCourseSection() + ")" ;

                PeerReviewSummaryDTO peerReviewSummaryDTO = new PeerReviewSummaryDTO();
                peerReviewSummaryDTO.setAssignmentTitle(assignment.getTitle());
                peerReviewSummaryDTO.setCourseCode(courseInfo);
                peerReviewSummaryDTO.setTotalReviews(totalReviews);
                peerReviewSummaryDTO.setCompletedReviews(completedReviewTasks);
                peerReviewSummaryDTO.setProgressPercentage(progress);
                peerReviewSummaryDTO.setTotalStudentCount(studentCount);

                result.add(peerReviewSummaryDTO);
            }

            return result;
        }), executor);

    }


    /**
     * Retrieves the assignment summary for a professor based on the provided filters.
     *
     * @param userId          The ID of the professor.
     * @param filterByCourseCode The course code to filter by (optional).
     * @param courseSection   The course section to filter by (optional).
     * @param term           The term to filter by (optional).
     * @return A CompletableFuture containing a list of Assignment objects.
     */
    public CompletableFuture<List<Assignment>> getAssignmentSummaryForProfessor(Long userId, String filterByCourseCode, String courseSection, String term) {
        return CompletableFuture.supplyAsync(()->jpaApi.withTransaction(entityManager -> {

            String queryString;
            TypedQuery<Assignment> query;

            if (filterByCourseCode != null && courseSection != null && term != null) {
                queryString = "SELECT a FROM Assignment a WHERE a.course.professor.userId = :userId AND a.course.courseCode = :courseCode AND a.course.courseSection = :courseSection AND a.course.term = :term ";
                query = entityManager.createQuery(queryString, Assignment.class)
                            .setParameter("userId", userId)
                            .setParameter("courseCode", filterByCourseCode)
                            .setParameter("courseSection", courseSection)
                            .setParameter("term", term);
            }
            else {
                queryString = "SELECT a FROM Assignment a WHERE a.course.professor.userId = :userId";
                query = entityManager.createQuery(queryString, Assignment.class)
                        .setParameter("userId", userId);
            }

            // Loop through each assignment and update the status value based on current data and fileUpload status and save the change as well to database
            List<Assignment> assignments = query.getResultList();
            for (Assignment assignment : assignments) {
                LocalDate today = LocalDate.now();
                LocalDate startDate = assignment.getStartDate();
                LocalDate dueDate = assignment.getDueDate();
                boolean fileUploaded = assignment.isPeerAssigned();

                if (!fileUploaded) {
                    assignment.setStatus(Status.PENDING);
                } else if (today.isBefore(startDate)) {
                    assignment.setStatus(Status.PENDING);
                } else if (today.isAfter(dueDate)) {
                    assignment.setStatus(Status.COMPLETED);
                } else {
                    assignment.setStatus(Status.ACTIVE);
                }

                entityManager.merge(assignment);
            }

            return assignments;
        }), executor);
    }


    /**
     * Retrieves the assignments for a student based on the provided course code.
     *
     * @param userId    The ID of the student.
     * @param courseCode The course code to filter by (optional).
     * @return A CompletableFuture containing a list of Assignment objects.
     */
    public CompletableFuture<List<Assignment>> getAssignmentsForStudent(Long userId, String courseCode) {
        return CompletableFuture.supplyAsync(() -> jpaApi.withTransaction(entityManager -> {
            if (courseCode == null || courseCode.equalsIgnoreCase("all")) {
                // Fetch the enrolled course codes for student
                TypedQuery<String> courseQuery = entityManager.createQuery(
                        "SELECT e.course.courseCode FROM Enrollment e WHERE e.student.userId = :userId", String.class);
                courseQuery.setParameter("userId", userId);
                List<String> courseCodes = courseQuery.getResultList();

                if (courseCodes.isEmpty()) return Collections.emptyList();

                // Fetch assignments for course codes
                TypedQuery<Assignment> assignmentQuery = entityManager.createQuery(
                        "SELECT a FROM Assignment a WHERE a.course.courseCode IN :courseCodes AND a.startDate <= CURRENT_DATE",
                        Assignment.class
                );
                assignmentQuery.setParameter("courseCodes", courseCodes);
                return assignmentQuery.getResultList();
            } else {
                // Fetch assignments for a specific course
                TypedQuery<Assignment> assignmentQuery = entityManager.createQuery(
                        "SELECT a FROM Assignment a WHERE a.course.courseCode = :courseCode AND a.startDate <= CURRENT_DATE",
                        Assignment.class
                );
                assignmentQuery.setParameter("courseCode", courseCode);
                return assignmentQuery.getResultList();
            }
        }), executor);
    }


    /**
     * Retrieves the pending peer reviews for a student based on the provided course code.
     *
     * @param userId    The ID of the student.
     * @param courseCode The course code to filter by (optional).
     * @return A CompletableFuture containing a list of ReviewTaskDTO objects.
     */
    public CompletableFuture<List<ReviewTaskDTO>> getPendingPeerReviewsForStudent(Long userId, String courseCode) {
        return CompletableFuture.supplyAsync(() -> jpaApi.withTransaction(entityManager -> {

            List<ReviewTask> reviewTasks;

            if (courseCode == null || courseCode.equalsIgnoreCase("all")) {
                // Fetch course codes the student is enrolled in
                List<String> enrolledCourses = entityManager.createQuery(
                                "SELECT e.course.courseCode FROM Enrollment e WHERE e.student.userId = :userId", String.class)
                        .setParameter("userId", userId)
                        .getResultList();

                if (enrolledCourses.isEmpty()) {
                    return Collections.emptyList();
                }

                reviewTasks = entityManager.createQuery(
                                "SELECT rt FROM ReviewTask rt " +
                                        "WHERE rt.reviewer.userId = :userId " +
                                        "AND rt.assignment.course.courseCode IN :courseCodes", ReviewTask.class)
                        .setParameter("userId", userId)
                        .setParameter("courseCodes", enrolledCourses)
                        .getResultList();
            } else {
                // Specific course
                reviewTasks = entityManager.createQuery(
                                "SELECT rt FROM ReviewTask rt " +
                                        "WHERE rt.reviewer.userId = :userId " +
                                        "AND rt.status = models.enums.Status.PENDING " +
                                        "AND rt.assignment.course.courseCode = :courseCode", ReviewTask.class)
                        .setParameter("userId", userId)
                        .setParameter("courseCode", courseCode)
                        .getResultList();
            }

               return reviewTasks.stream()
                    .map(rt ->{

                        List<FeedbackDTO> feedbacks = rt.getFeedbacks()
                                .stream()
                                .map(feedback -> new FeedbackDTO(
                                        feedback.getId(),
                                        feedback.getScore(),
                                        feedback.getQuestion().getMaxMarks(),
                                        feedback.getQuestion().getQuestionText(),
                                        feedback.getFeedbackText()
                                ))
                                .toList();

                        return new ReviewTaskDTO(
                                rt.getReviewTaskId(),
                                rt.getAssignment().getAssignmentId(),
                                rt.getAssignment().getDueDate(),
                                rt.getAssignment().getCourse().getCourseCode(),
                                rt.getAssignment().getTitle(),
                                rt.getReviewee().getUserName(),
                                rt.getStatus(),
                                feedbacks,
                                rt.isReviewTaskForProfessor());
                    }).toList();
        }), executor);
    }
}
