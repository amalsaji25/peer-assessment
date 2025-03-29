package services.core;

import forms.AssignmentForm;
import models.ReviewTask;
import models.dto.AssignmentEditDTO;
import models.dto.AssignmentExportDTO;
import models.dto.AssignmentUploadContext;
import play.libs.Files;
import play.mvc.Http;
import play.mvc.Result;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface AssignmentService {
    CompletableFuture<Integer> getAssignmentCountByProfessorId(Long userId, String courseCode);

    CompletableFuture<Result> createAssignment(AssignmentForm assignmentForm, List<ReviewTask> reviewTasks);

    CompletableFuture<AssignmentForm> parseAssignmentForm(Http.MultipartFormData assignmentFormData);

    CompletableFuture<List<ReviewTask>> parseAssignmentTaskTeamInfo(AssignmentUploadContext context);

    CompletableFuture<AssignmentEditDTO> getAssignmentDetails(Long assignmentId);

    CompletionStage<Void> updateAssignment(Long assignmentId, AssignmentForm form);

    CompletableFuture<List<Map<String,Object>>> fetchAssignmentsForCourse(String courseId);

    CompletableFuture<Boolean> deleteAssignment(Long assignmentId);
}
