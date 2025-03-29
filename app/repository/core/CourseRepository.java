package repository.core;

import jakarta.persistence.TypedQuery;
import models.Course;
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
public class CourseRepository implements Repository<Course> {

    private final JPAApi jpaApi;
    private static final Logger log = LoggerFactory.getLogger(CourseRepository.class);
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Inject
    public CourseRepository(JPAApi jpaApi) {
        this.jpaApi = jpaApi;
    }

    public Optional<Course> findByCourseCode(String courseCode) {
        try {
            return jpaApi.withTransaction(entityManager -> {
                Course course = entityManager.find(Course.class, courseCode);
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
    public CompletionStage<Map<String, Object>> saveAll(List<Course> cours) {
        int batchSize = 100;

        // Split into batches
        List<List<Course>> batches = new ArrayList<>();
        for (int i = 0; i < cours.size(); i += batchSize) {
            batches.add(cours.subList(i, Math.min(i + batchSize, cours.size())));
        }

        // Process each batch asynchronously
        List<CompletableFuture<Map<String, Object>>> futures = batches.stream()
                .map(batch -> CompletableFuture.supplyAsync(() -> {
                    return jpaApi.withTransaction(entityManager -> {
                        int successCount = 0;
                        List<String> failedRecords = new ArrayList<>();

                        for (Course course : batch) {
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

    public CompletableFuture<Integer> findActiveCoursesByProfessorId(Long userId, String courseCode) {
        return CompletableFuture.supplyAsync(() -> jpaApi.withTransaction(entityManager -> {

            String queryString;
            TypedQuery<Course> query;

            if(courseCode != null){
                queryString = "SELECT c FROM Course c "+
                              "WHERE c.professor.userId = :userId AND c.courseCode = :courseCode";
                query = entityManager.createQuery(queryString, Course.class)
                        .setParameter("userId", userId)
                        .setParameter("courseCode", courseCode);
            }
            else{
                queryString = "SELECT c FROM Course c WHERE c.professor.userId = :userId";

                query = entityManager.createQuery(queryString, Course.class)
                        .setParameter("userId", userId);
            }
            return query.getResultList().size();
        }), executorService);
    }

    public CompletableFuture<List<Map<String, String>>> findAllCourses(Long userId) {
        return CompletableFuture.supplyAsync(() -> jpaApi.withTransaction(entityManager -> {
            List<Course> courses = entityManager.createQuery(
                            "SELECT c FROM Course c WHERE c.professor.userId = :userId", Course.class)
                    .setParameter("userId", userId)
                    .getResultList();

            List<Map<String, String>> courseList = new ArrayList<>();

            for (Course course : courses) {
                Map<String, String> courseData = new HashMap<>();
                courseData.put("id", course.getCourseCode());
                courseData.put("code", course.getCourseCode());
                courseData.put("name", course.getCourseName());
                courseList.add(courseData);
            }

            return courseList;
        }));
    }
}