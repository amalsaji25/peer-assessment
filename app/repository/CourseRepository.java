package repository;

import models.Courses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Singleton
public class CourseRepository implements Repository<Courses> {

    private final JPAApi jpaApi;
    private static final Logger log = LoggerFactory.getLogger(CourseRepository.class);
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Inject
    public CourseRepository(JPAApi jpaApi) {
        this.jpaApi = jpaApi;
    }

    public Optional<Courses> findByCourseCode(String courseCode) {
        try {
            return jpaApi.withTransaction(entityManager -> {
                Courses course = entityManager.find(Courses.class, courseCode);
                if (course != null) {
                    log.info("Course {} found", course.getCourseCode());
                    return Optional.of(course);
                } else {
                    log.warn("Course {} not found", courseCode);
                    return Optional.empty();
                }
            });
        } catch (Exception e) {
            log.error("findByCourseCode failed for course {} with exception: {}", courseCode, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public CompletionStage<Map<String, Object>> saveAll(List<Courses> courses) {
        int batchSize = 100;

        // Split into batches
        List<List<Courses>> batches = new ArrayList<>();
        for (int i = 0; i < courses.size(); i += batchSize) {
            batches.add(courses.subList(i, Math.min(i + batchSize, courses.size())));
        }

        // Process each batch asynchronously
        List<CompletableFuture<Map<String, Object>>> futures = batches.stream()
                .map(batch -> CompletableFuture.supplyAsync(() -> {
                    return jpaApi.withTransaction(entityManager -> {
                        int successCount = 0;
                        List<String> failedRecords = new ArrayList<>();

                        for (Courses course : batch) {
                            try {
                                entityManager.persist(course);
                                successCount++;
                            } catch (Exception e) {
                                failedRecords.add(course.getCourseCode());
                                log.error("Failed to save course {} - {}", course.getCourseCode(), e.getMessage());
                            }
                        }

                        entityManager.flush();
                        entityManager.clear();

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
}