package repository.core;

import jakarta.persistence.TypedQuery;
import models.Course;
import models.dto.Context;
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

    public Optional<Course> findByCourseCodeAndSectionAndTerm(String courseCode, String courseSection, String term) {
        try {
            return jpaApi.withTransaction(
                    entityManager -> {
                        TypedQuery<Course> query = entityManager.createQuery(
                                "SELECT c FROM Course c WHERE c.courseCode = :courseCode AND c.courseSection = :courseSection AND c.term = :term", Course.class);
                        query.setParameter("courseCode", courseCode);
                        query.setParameter("term", term);
                        query.setParameter("courseSection", courseSection);

                        List<Course> resultList = query.getResultList();
                        if (resultList.isEmpty()) {
                            log.warn("Course with code {} and term {} not found", courseCode, term);
                            return Optional.empty();
                        } else {
                            return Optional.of(resultList.get(0));
                        }
                    }
            );
        } catch (Exception e) {
            log.error("findByCourseCodeAndSectionAndTerm failed for course {} with exception: {}", courseCode, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public CompletionStage<Map<String, Object>> saveAll(List<Course> courses, Context context) {
        int batchSize = 100;

        // Split into batches
        List<List<Course>> batches = new ArrayList<>();
        for (int i = 0; i < courses.size(); i += batchSize) {
            batches.add(courses.subList(i, Math.min(i + batchSize, courses.size())));
        }

        // Process each batch asynchronously
        List<CompletableFuture<Map<String, Object>>> futures = batches.stream()
                .map(batch -> CompletableFuture.supplyAsync(() -> {
                    return jpaApi.withTransaction(entityManager -> {
                        int successCount = 0;
                        int skippedCount = 0;
                        List<String> failedRecords = new ArrayList<>();

                        for (Course course : batch) {
                            try {
                                Course existing = entityManager.createQuery(
                                                "SELECT c FROM Course c WHERE c.courseCode = :code AND c.term = :term AND c.courseSection = :courseSection", Course.class)
                                        .setParameter("code", course.getCourseCode())
                                        .setParameter("term", course.getTerm())
                                        .setParameter("courseSection", course.getCourseSection())
                                        .getResultStream()
                                        .findFirst()
                                        .orElse(null);

                                if (existing != null) {
                                    skippedCount++;
                                    log.info("Course {} for term {} already exists. Skipping.", course.getCourseCode(), course.getTerm());
                                    continue;
                                }

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
                        response.put("skippedCount", skippedCount);
                        response.put("failedCount", failedRecords.size());
                        response.put("failedRecords", failedRecords);
                        return response;
                    });
                }, executorService))
                .collect(Collectors.toList());

        // Aggregate batch results
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    int totalSuccess = futures.stream()
                            .mapToInt(f -> (Integer) f.join().get("successCount"))
                            .sum();

                    int totalSkipped = futures.stream()
                            .mapToInt(f -> (Integer) f.join().get("skippedCount"))
                            .sum();

                    int totalFailed = futures.stream()
                            .mapToInt(f -> (Integer) f.join().get("failedCount"))
                            .sum();

                    List<String> allFailedRecords = futures.stream()
                            .flatMap(f -> ((List<String>) f.join().get("failedRecords")).stream())
                            .collect(Collectors.toList());

                    Map<String, Object> finalResponse = new HashMap<>();
                    finalResponse.put("successCount", totalSuccess);
                    finalResponse.put("skippedCount", totalSkipped);
                    finalResponse.put("failedCount", totalFailed);
                    finalResponse.put("failedRecords", allFailedRecords);
                    return finalResponse;
                });
    }

    public CompletableFuture<Integer> findActiveCoursesByProfessorId(Long userId, String courseCode, String courseSection, String term) {
        return CompletableFuture.supplyAsync(() -> jpaApi.withTransaction(entityManager -> {

            String queryString;
            TypedQuery<Course> query;

            if(courseCode != null){
                queryString = "SELECT c FROM Course c "+
                              "WHERE c.professor.userId = :userId AND c.courseCode = :courseCode AND c.term = :term AND c.courseSection = :courseSection";
                query = entityManager.createQuery(queryString, Course.class)
                        .setParameter("userId", userId)
                        .setParameter("courseCode", courseCode)
                        .setParameter("courseSection", courseSection)
                        .setParameter("term", term);
            }
            else{
                queryString = "SELECT c FROM Course c WHERE c.professor.userId = :userId";

                query = entityManager.createQuery(queryString, Course.class)
                        .setParameter("userId", userId);
            }
            return query.getResultList().size();
        }), executorService);
    }

    public CompletableFuture<List<Map<String, String>>> findAllCourses(Long userId, String term) {

        return CompletableFuture.supplyAsync(() -> jpaApi.withTransaction(entityManager -> {
            String queryString = "SELECT c FROM Course c WHERE c.professor.userId = :userId";
            if (!term.equalsIgnoreCase("all")) {
                queryString += " AND c.term = :term";
            }
            TypedQuery<Course> query = entityManager.createQuery(queryString, Course.class)
                    .setParameter("userId", userId);
            if (!term.equalsIgnoreCase("all")) {
                query.setParameter("term", term);
            }

            List<Course> courses = query.getResultList();

            List<Map<String, String>> courseList = new ArrayList<>();

            for (Course course : courses) {
                Map<String, String> courseData = new HashMap<>();
                courseData.put("code", course.getCourseCode());
                courseData.put("name", course.getCourseName());
                courseData.put("section", course.getCourseSection());
                courseData.put("term", course.getTerm());
                courseList.add(courseData);
            }

            return courseList;
        }), executorService);

//        return CompletableFuture.supplyAsync(() -> jpaApi.withTransaction(entityManager -> {
//            List<Course> courses = entityManager.createQuery(
//                            "SELECT c FROM Course c WHERE c.professor.userId = :userId", Course.class)
//                    .setParameter("userId", userId)
//                    .getResultList();
//
//            List<Map<String, String>> courseList = new ArrayList<>();
//
//            for (Course course : courses) {
//                Map<String, String> courseData = new HashMap<>();
//                courseData.put("code", course.getCourseCode());
//                courseData.put("name", course.getCourseName());
//                courseData.put("section", course.getCourseSection());
//                courseData.put("term", course.getTerm());
//                courseList.add(courseData);
//            }
//
//            return courseList;
//        }));
    }

    public CompletableFuture<List<String>> findAllTerms(Long userId) {
        return CompletableFuture.supplyAsync(() -> jpaApi.withTransaction(entityManager -> {
            return entityManager.createQuery(
                            "SELECT DISTINCT c.term FROM Course c WHERE c.professor.userId = :userId", String.class)
                    .setParameter("userId", userId)
                    .getResultList();
        }));
    }

    public void updateIsPeerAssignedFlagForCourse(String courseCode, String courseSection, String term) {
            jpaApi.withTransaction(entityManager -> {
                Course course = entityManager.createQuery(
                                "SELECT c FROM Course c WHERE c.courseCode = :courseCode AND c.courseSection = :courseSection AND c.term = :term", Course.class)
                        .setParameter("courseCode", courseCode)
                        .setParameter("courseSection", courseSection)
                        .setParameter("term", term)
                        .getSingleResult();
                course.setStudentFileUploaded(true);
                entityManager.merge(course);
            });
    }
}