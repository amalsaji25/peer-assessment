package services.core;

import models.Feedback;

import java.util.List;
import java.util.Optional;

public interface FeedbackService {
    Optional<List<Feedback>> getFeedbacksForReviewTaskId(Long reviewTaskId);
}
