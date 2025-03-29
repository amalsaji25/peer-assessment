package services.core;

import models.Feedback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.core.FeedbackRepository;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class FeedbackServiceImpl implements FeedbackService{

    private final FeedbackRepository feedbackRepository;
    private static final Logger log = LoggerFactory.getLogger(FeedbackServiceImpl.class);

    @Inject
    public FeedbackServiceImpl(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    @Override
    public Optional<List<Feedback>> getFeedbacksForReviewTaskId(Long reviewTaskId) {
        return feedbackRepository.findFeedbacksReviewTaskId(reviewTaskId);
    }
}
