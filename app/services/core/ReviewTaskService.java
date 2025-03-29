package services.core;

import models.ReviewTask;
import models.dto.SubmissionOverviewDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ReviewTaskService {
    CompletableFuture<SubmissionOverviewDTO> getReviewTasksSubmissionOverview(Long assignmentId);

    CompletableFuture<List<ReviewTask>> getReviewTasks(Long assignmentId);
}
