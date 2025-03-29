package repository;

import jakarta.persistence.TypedQuery;
import models.Assignment;
import models.dto.AssignmentSummaryDTO;
import models.dto.PeerReviewSummaryDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
public class DashboardRepository {
    private final JPAApi jpaApi;
    private static final Logger log = LoggerFactory.getLogger(DashboardRepository.class);
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    @Inject
    public DashboardRepository(JPAApi jpaApi) {
        this.jpaApi = jpaApi;
    }


    public CompletableFuture<List<PeerReviewSummaryDTO>> getPeerReviewProgressForProfessor(Long userId, String filterByCourseCode) {
        return CompletableFuture.supplyAsync(()->jpaApi.withTransaction(entityManger -> {

            List<PeerReviewSummaryDTO> result = new ArrayList<>();

            String queryString;
            TypedQuery<Assignment> query;

            if(filterByCourseCode != null){
                queryString = "SELECT a FROM Assignment a WHERE a.course.courseCode = :courseCode AND a.course.professor.userId = :userId ";
                query = entityManger.createQuery(queryString, Assignment.class)
                        .setParameter("courseCode", filterByCourseCode)
                        .setParameter("userId", userId);
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

                int totalReviews = entityManger.createQuery("SELECT COUNT(r) FROM ReviewTask r WHERE r.assignment.assignmentId = :assignmentId", Long.class)
                        .setParameter("assignmentId", assignmentId)
                        .getSingleResult()
                        .intValue();

                int completedReviews = entityManger.createQuery("SELECT COUNT(r) FROM ReviewTask r WHERE r.assignment.assignmentId = :assignmentId AND r.status = 'COMPLETED'", Long.class)
                        .setParameter("assignmentId", assignmentId)
                        .getSingleResult()
                        .intValue();

                // Get the total number of students enrolled in the course

                int studentCount = entityManger.createQuery("SELECT COUNT(e) FROM Enrollment e WHERE e.course.courseCode = :courseCode", Long.class)
                        .setParameter("courseCode", assignment.getCourse().getCourseCode())
                        .getSingleResult()
                        .intValue();


                int progress = totalReviews == 0 ? 0 : (completedReviews * 100) / totalReviews;

                PeerReviewSummaryDTO peerReviewSummaryDTO = new PeerReviewSummaryDTO();
                peerReviewSummaryDTO.setAssignmentTitle(assignment.getTitle());
                peerReviewSummaryDTO.setCourseCode(courseCode);
                peerReviewSummaryDTO.setTotalReviews(totalReviews);
                peerReviewSummaryDTO.setCompletedReviews(completedReviews);
                peerReviewSummaryDTO.setProgressPercentage(progress);
                peerReviewSummaryDTO.setTotalStudentCount(studentCount);

                result.add(peerReviewSummaryDTO);
            }

            return result;
        }), executor);

    }

    public CompletableFuture<List<Assignment>> getAssignmentSummaryForProfessor(Long userId, String filterByCourseCode) {
        return CompletableFuture.supplyAsync(()->jpaApi.withTransaction(entityManager -> {

            String queryString;
            TypedQuery<Assignment> query;

            if (filterByCourseCode != null) {
                queryString = "SELECT a FROM Assignment a WHERE a.course.professor.userId = :userId AND a.course.courseCode = :courseCode";
                query = entityManager.createQuery(queryString, Assignment.class)
                            .setParameter("userId", userId)
                            .setParameter("courseCode", filterByCourseCode);
            }
            else {
                queryString = "SELECT a FROM Assignment a WHERE a.course.professor.userId = :userId";
                query = entityManager.createQuery(queryString, Assignment.class)
                        .setParameter("userId", userId);
            }

            return query.getResultList();
        }), executor);
    }
}
