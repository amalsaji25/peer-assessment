package services.core;

import forms.AssignmentForm;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import models.Assignment;
import models.dto.AssignmentEditDTO;
import play.mvc.Http;
import play.mvc.Result;

/**
 * AssignmentService is an interface that defines methods for managing assignments in a course
 * management system. It provides methods to create, update, delete, and retrieve assignments.
 */
public interface AssignmentService {
  CompletableFuture<Integer> getAssignmentCountByProfessorId(
      Long userId, String courseCode, String courseSection, String term);

  CompletableFuture<Result> createAssignment(AssignmentForm assignmentForm);

  CompletableFuture<AssignmentForm> parseAssignmentForm(Http.MultipartFormData assignmentFormData);

  CompletableFuture<AssignmentEditDTO> getAssignmentDetails(Long assignmentId);

  CompletionStage<Void> updateAssignment(Long assignmentId, AssignmentForm form);

  CompletableFuture<List<Map<String, Object>>> fetchAssignmentsForCourse(String courseId);

  CompletableFuture<Boolean> deleteAssignment(Long assignmentId);

  CompletableFuture<Integer> getAssignmentCountByStudentId(Long userId, String courseCode);

  Optional<Assignment> getAssignment(Long assignmentId);
}
