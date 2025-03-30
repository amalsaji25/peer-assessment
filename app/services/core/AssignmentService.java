package services.core;

import forms.AssignmentForm;
import models.ReviewTask;
import models.dto.AssignmentEditDTO;
import models.dto.Context;
import play.mvc.Http;
import play.mvc.Result;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface AssignmentService {
    CompletableFuture<Integer> getAssignmentCountByProfessorId(Long userId, String courseCode, String courseSection, String term);

    CompletableFuture<Result> createAssignment(AssignmentForm assignmentForm);

    CompletableFuture<AssignmentForm> parseAssignmentForm(Http.MultipartFormData assignmentFormData);

    CompletableFuture<AssignmentEditDTO> getAssignmentDetails(Long assignmentId);

    CompletionStage<Void> updateAssignment(Long assignmentId, AssignmentForm form);

    CompletableFuture<List<Map<String,Object>>> fetchAssignmentsForCourse(String courseId);

    CompletableFuture<Boolean> deleteAssignment(Long assignmentId);

    CompletableFuture<Integer> getAssignmentCountByStudentId(Long userId, String courseCode);
}
