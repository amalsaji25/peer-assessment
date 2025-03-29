package repository.core;

import jakarta.persistence.TypedQuery;
import models.Enrollment;
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
    public CompletionStage<Map<String, Object>> saveAll(List<Enrollment> enrollments) {
        int batchSize = 100;

        // Split into batches
        List<List<Enrollment>> batches = new ArrayList<>();
        for (int i = 0; i < enrollments.size(); i += batchSize) {
            batches.add(enrollments.subList(i, Math.min(i + batchSize, enrollments.size())));
        }

        // Process each batch asynchronously
        List<CompletableFuture<Map<String, Object>>> futures = batches.stream()
                .map(batch -> CompletableFuture.supplyAsync(() -> {
                    return jpaApi.withTransaction(entityManager -> {
                        int successCount = 0;
                        List<String> failedRecords = new ArrayList<>();

                        for (Enrollment enrollment : batch) {
                            try {
                                entityManager.persist(enrollment);
                                successCount++;
                            } catch (Exception e) {
                                failedRecords.add("Student: " + enrollment.getStudent().getUserId() +
                                        ", Course: " + enrollment.getCourse().getCourseCode());
                                log.error("Failed to save enrollment for Student: {} in Course: {} - {}",
                                        enrollment.getStudent().getUserId(), enrollment.getCourse().getCourseCode(), e.getMessage());
                            }
                        }

                        entityManager.flush();
                        entityManager.clear(); // Helps with memory management

                        // Return structured response
                        Map<String, Object> response = new HashMap<>();
                        response.put("successCount", successCount);
                        response.put("failedCount", failedRecords.size());
                        response.put("failedRecords", failedRecords);
                        return response;
                    });
                }, executorService))
                .collect(Collectors.toList());

        // Wait for all futures to complete and aggregate results
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    int totalSuccess = futures.stream()
                            .mapToInt(f -> (Integer) f.join().get("successCount"))
                            .sum();

                    int totalFailed = futures.stream()
                            .mapToInt(f -> (Integer) f.join().get("failedCount"))
                            .sum();

                    List<String> allFailedRecords = futures.stream()
                            .flatMap(f -> ((List<String>) f.join().get("failedRecords")).stream())
                            .collect(Collectors.toList());

                    Map<String, Object> finalResponse = new HashMap<>();
                    finalResponse.put("successCount", totalSuccess);
                    finalResponse.put("failedCount", totalFailed);
                    finalResponse.put("failedRecords", allFailedRecords);
                    return finalResponse;
                });
    }

    public CompletableFuture<Integer> getStudentCountByProfessorId(Long professorId, String courseCode) {
    return CompletableFuture.supplyAsync(
        () ->
            jpaApi.withTransaction(
                entityManager -> {

                  String queryString;
                  TypedQuery<Long> query;
                  if (courseCode != null) {
                    queryString =
                        "SELECT COUNT(e.student) FROM Enrollment e "
                            + "WHERE e.course.professor.userId=:professorId AND e.course.courseCode=:courseCode";
                    query =
                        entityManager
                            .createQuery(queryString, Long.class)
                            .setParameter("professorId", professorId)
                            .setParameter("courseCode", courseCode);
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
}