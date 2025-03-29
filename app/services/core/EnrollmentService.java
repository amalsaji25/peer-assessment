package services.core;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface EnrollmentService {
    CompletableFuture<Integer> getStudentCountByProfessorId(Long userId, String courseCode);
}
