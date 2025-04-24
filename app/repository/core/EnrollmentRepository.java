package repository.core;

import jakarta.persistence.TypedQuery;
import java.util.*;
import java.util.concurrent.*;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.Enrollment;
import models.dto.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.jpa.JPAApi;

/**
 * EnrollmentRepository is a singleton class that handles the persistence of Enrollment entities in
 * the database. It provides methods to save, retrieve, and query enrollments.
 */
@Singleton
public class EnrollmentRepository implements Repository<Enrollment> {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentRepository.class);
    private final JPAApi jpaApi;
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Inject
    public EnrollmentRepository(JPAApi jpaApi) {
        this.jpaApi = jpaApi;
    }

    /**
     * Saves a single enrollment to the database.
     * @param enrollments enrollment object to be saved
     * @param context context object containing course information
     * @return a CompletableFuture containing the saved enrollment object
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

    /**
     * Retrieves all enrollments from the database for a given course code, course section, term and professor ID. To find the enrollment count for a specific professor, course code, course section, and term.
     * @param professorId the ID of the professor
     * @param courseCode the course code
     * @param courseSection the course section
     * @param term the term
     * @return a CompletableFuture containing the count of enrollments
     */
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

    /**
     * Retrieves all enrollments from the database for a given course code, course section, term and student ID.
     * @param userId the ID of the student
     * @return  a CompletableFuture containing a list of course codes
     */
    public CompletableFuture<List<Long>> findCourseCodesByStudentId(Long userId){
        return CompletableFuture.supplyAsync(
                () ->
                        jpaApi.withTransaction(
                                entityManager -> {
                                    String queryString = "SELECT e.course.courseId FROM Enrollment e WHERE e.student.userId=:userId";
                                    TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);
                                    query.setParameter("userId", userId);
                                    return query.getResultList();
                                }),
                executorService
        );
    }

    /**
     * Retrieves all enrollments from the database for a given course code, course section, term and student ID.
     * @param userId the ID of the student
     * @return  a CompletableFuture containing a list of maps with course code and course name
     */
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

    /**
     * Checks if a student is enrolled in a specific course.
     * @param userId the ID of the student
     * @param courseCode the course code
     * @return a CompletableFuture containing true if the student is enrolled, false otherwise
     */
    public CompletableFuture<Optional<Long>> isStudentEnrolledInCourse(Long userId, String courseCode){
        return CompletableFuture.supplyAsync(
                () ->
                        jpaApi.withTransaction(
                                entityManager -> {
                                    String queryString = "SELECT e.course.courseId FROM Enrollment e WHERE e.student.userId=:userId AND e.course.courseCode=:courseCode";
                                    TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);
                                    query.setParameter("userId", userId);
                                    query.setParameter("courseCode", courseCode);
                                    List<Long> result = query.getResultList();
                                    return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
                                }),
                executorService
        );

    }

    /**
     * Retrieves all enrollments from the database for a given course code, course section, term and student ID.
     * @param userIds the IDs of the students
     * @param courseCode the course code
     * @param courseSection the course section
     * @param term the term
     * @return a list of enrollments
     */
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