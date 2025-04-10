package services.core;

import com.fasterxml.jackson.databind.JsonNode;
import models.ReviewTask;
import models.dto.GroupSubmissionDTO;
import models.dto.SubmissionOverviewDTO;
import models.enums.Status;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface ReviewTaskService {
    CompletableFuture<SubmissionOverviewDTO> getReviewTasksSubmissionOverview(Long assignmentId);

    void calculateClassAverages(List<GroupSubmissionDTO> groupDTOs);

    CompletableFuture<List<ReviewTask>> getReviewTasks(Long assignmentId);

    CompletableFuture<Integer> getReviewCountByStatus(Long userId, String courseCode, Status status);

    CompletableFuture<String> parseAndSaveOrSubmitReviewTask(Long reviewTaskId, JsonNode json);

    Map<Long, List<ReviewTask>> groupReviewTasksByGroup(List<ReviewTask> reviewTasks);

    List<GroupSubmissionDTO> generateSubmissionInfoInEachGroupDTOs(Map<Long, List<ReviewTask>> groupedReviewTasks);
}
