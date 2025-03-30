package services.core;

import models.Feedback;
import models.dto.FeedbacksReceivedDTO;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface FeedbackService {
    Optional<List<Feedback>> getFeedbacksForReviewTaskId(Long reviewTaskId);

    CompletableFuture<List<FeedbacksReceivedDTO>> getFeedbacksReceivedByStudent(Long userId, String courseCode);
}
