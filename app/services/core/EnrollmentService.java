package services.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface EnrollmentService {
    CompletableFuture<Integer> getStudentCountByProfessorId(Long userId, String courseCode, String courseSection, String term);

    CompletableFuture<List<Long>> findStudentEnrolledCourseCodes(Long userId, String courseCode);

    CompletableFuture<List<Map<String, String>>> getStudentEnrolledCourse(Long userId);
}
