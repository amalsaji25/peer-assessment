package repository.core;

import jakarta.persistence.TypedQuery;
import models.Enrollment;
import models.dto.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Singleton
public class EnrollmentRepository implements Repository<Enrollment> {

    private final JPAApi jpaApi;
    private static final Logger log = LoggerFactory.getLogger(EnrollmentRepository.class);
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Inject
    public EnrollmentRepository(JPAApi jpaApi) {
        this.jpaApi = jpaApi;
    }

    /**
     * Bulk save enrollments using batch processing.
     */
    @Override
    public CompletionStage<Map<String, Object>> saveAll(List<Enrollment> enrollments, Context context) {
        return CompletableFuture.supplyAsync(() -> {
            int successCount = 0;
            int failedCount = 0;
            List<String> failedRecords = new ArrayList<>();

            for (Enrollment enrollment : enrollments) {
                try {
                    jpaApi.withTransaction(entityManager -> {
                        entityManager.persist(enrollment);
                    });
                    successCount++;
                } catch (Exception e) {
                    failedCount++;
                    String errorMsg = String.format("Student: %s, Course: %s",
                            enrollment.getStudent().getUserId(),
                            enrollment.getCourse().getCourseCode());
                    failedRecords.add(errorMsg);
                    log.error("Failed to save enrollment for {} - {}", errorMsg, e.getMessage(), e);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("successCount", successCount);
            result.put("failedCount", failedCount);
            result.put("failedRecords", failedRecords);
            return result;
        });
    }

    public CompletableFuture<Integer> getStudentCountByProfessorId(Long professorId, String courseCode, String courseSection, String term) {
    return CompletableFuture.supplyAsync(
        () ->
            jpaApi.withTransaction(
                entityManager -> {

                  String queryString;
                  TypedQuery<Long> query;
                  if (courseCode != null && courseSection != null && term != null) {
                    queryString =
                        "SELECT COUNT(e.student) FROM Enrollment e "
                            + "WHERE e.course.professor.userId=:professorId AND e.course.courseCode=:courseCode AND e.course.courseSection=:courseSection AND e.course.term=:term";
                    query =
                        entityManager
                            .createQuery(queryString, Long.class)
                            .setParameter("professorId", professorId)
                            .setParameter("courseCode", courseCode)
                            .setParameter("courseSection", courseSection)
                             .setParameter("term", term);
                  }
                  else{
                      queryString = "SELECT COUNT(e.student) FROM Enrollment e " +
                              "WHERE e.course.professor.userId=:professorId";

                      query =
                              entityManager
                                      .createQuery(queryString, Long.class)
                                      .setParameter("professorId", professorId);
                  }

                  return query.getSingleResult().intValue();
                }),
        executorService);
    }

    public CompletableFuture<List<String>> findCourseCodesByStudentId(Long userId){
        return CompletableFuture.supplyAsync(
                () ->
                        jpaApi.withTransaction(
                                entityManager -> {
                                    String queryString = "SELECT e.course.courseCode FROM Enrollment e WHERE e.student.userId=:userId";
                                    TypedQuery<String> query = entityManager.createQuery(queryString, String.class);
                                    query.setParameter("userId", userId);
                                    return query.getResultList();
                                }),
                executorService
        );
    }

    public CompletableFuture<List<Map<String,String>>> findEnrolledCoursesForStudentId(Long userId){
        return CompletableFuture.supplyAsync(
                () ->
                        jpaApi.withTransaction(
                                entityManager -> {
                                    String queryString = "SELECT e.course.courseCode, e.course.courseName FROM Enrollment e WHERE e.student.userId=:userId";
                                    TypedQuery<Object[]> query = entityManager.createQuery(queryString, Object[].class);
                                    query.setParameter("userId", userId);
                                    List<Object[]> results = query.getResultList();

                                    List<Map<String, String>> courseList = new ArrayList<>();
                                    for (Object[] result : results) {
                                        Map<String, String> courseData = new HashMap<>();
                                        courseData.put("courseCode", (String) result[0]);
                                        courseData.put("courseName", (String) result[1]);
                                        courseList.add(courseData);
                                    }
                                    return courseList;
                                }),
                executorService
        );
    }

    public CompletableFuture<Boolean> isStudentEnrolledInCourse(Long userId, String courseCode){
        return CompletableFuture.supplyAsync(
                () ->
                        jpaApi.withTransaction(
                                entityManager -> {
                                    String queryString = "SELECT COUNT(e) FROM Enrollment e WHERE e.student.userId=:userId AND e.course.courseCode=:courseCode";
                                    TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);
                                    query.setParameter("userId", userId);
                                    query.setParameter("courseCode", courseCode);
                                    return query.getSingleResult() > 0;
                                }),
                executorService
        );

    }

    public List<Enrollment> getEnrollmentsByCompositeIndex(
            List<Long> userIds, String courseCode, String courseSection, String term) {
        return jpaApi.withTransaction(entityManager -> {
            TypedQuery<Enrollment> query = entityManager.createQuery(
                    "SELECT e FROM Enrollment e WHERE " +
                            "e.student.userId IN :userIds AND " +
                            "e.course.courseCode = :courseCode AND " +
                            "e.course.courseSection = :courseSection AND " +
                            "e.course.term = :term",
                    Enrollment.class);
            query.setParameter("userIds", userIds);
            query.setParameter("courseCode", courseCode);
            query.setParameter("courseSection", courseSection);
            query.setParameter("term", term);
            return query.getResultList();
        });
    }

}