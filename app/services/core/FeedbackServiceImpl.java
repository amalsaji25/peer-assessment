package services.core;

import models.Feedback;
import models.ReviewTask;
import models.dto.FeedbackDTO;
import models.dto.FeedbacksReceivedDTO;
import models.enums.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.core.FeedbackRepository;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class FeedbackServiceImpl implements FeedbackService{

    private final FeedbackRepository feedbackRepository;
    private final EnrollmentService enrollmentService;
    private static final Logger log = LoggerFactory.getLogger(FeedbackServiceImpl.class);

    @Inject
    public FeedbackServiceImpl(FeedbackRepository feedbackRepository, EnrollmentService enrollmentService) {
        this.feedbackRepository = feedbackRepository;
        this.enrollmentService = enrollmentService;
    }

    @Override
    public Optional<List<Feedback>> getFeedbacksForReviewTaskId(Long reviewTaskId) {
        return feedbackRepository.findFeedbacksReviewTaskId(reviewTaskId);
    }

    @Override
    public CompletableFuture<List<FeedbacksReceivedDTO>> getFeedbacksReceivedByStudent(Long userId, String courseCode) {
        return enrollmentService.findStudentEnrolledCourseCodes(userId, courseCode)
                .thenCompose(courseCodes -> feedbackRepository.findFeedbacksReceivedByStudent(userId, courseCodes))
                .thenApply(feedbacks ->{
                    Map<ReviewTask, List<Feedback>> groupedFeedbacks = feedbacks.stream()
                            .filter(feedback -> feedback.getReviewTask().getStatus() == Status.COMPLETED)
                            .collect(Collectors.groupingBy(Feedback::getReviewTask));

                    List<FeedbacksReceivedDTO> feedbacksReceivedDTOS = new ArrayList<>();
                    AtomicInteger peerCounter = new AtomicInteger(1);

                    for(Map.Entry<ReviewTask, List<Feedback>> entry : groupedFeedbacks.entrySet()) {
                        ReviewTask reviewTask = entry.getKey();
                        List<Feedback> groupFeedbacks = entry.getValue();

                        List<FeedbackDTO> feedbackDTOs = groupFeedbacks.stream()
                                .map(feedback -> new FeedbackDTO(feedback.getScore(), feedback.getFeedbackText(),feedback.getQuestion().getQuestionText()))
                                .toList();

                        int totalMarks = groupFeedbacks.stream()
                                .mapToInt(feedback -> feedback.getQuestion().getMaxMarks())
                                .sum();
                        int marksObtained = groupFeedbacks.stream()
                                .mapToInt(Feedback::getScore)
                                .reduce(0, Integer::sum);


                        feedbacksReceivedDTOS.add(new FeedbacksReceivedDTO(reviewTask.getAssignment().getTitle(),
                                "Anonymous Peer " + peerCounter.getAndIncrement(),
                                totalMarks,
                                marksObtained,
                                feedbackDTOs));
                    }
                    return feedbacksReceivedDTOS;
                });
    }
}
