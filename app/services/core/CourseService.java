package services.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface CourseService {
    CompletableFuture<Integer> getActiveCoursesByProfessorId(Long userId, String courseCode, String courseSection, String term);

    CompletableFuture<List<Map<String, String>>> getAllCourses(Long userId, String term);

    CompletableFuture<List<String>> getAllTerms(Long userId);
}
