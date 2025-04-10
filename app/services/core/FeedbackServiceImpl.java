package services.core;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.inject.Inject;
import models.Assignment;
import models.Feedback;
import models.ReviewTask;
import models.dto.FeedbacksReceivedDTO;
import models.enums.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.core.FeedbackRepository;

/**
 * FeedbackServiceImpl is a service class that implements the FeedbackService interface. It provides
 * methods to interact with the FeedbackRepository for managing feedbacks in the system.
 */
public class FeedbackServiceImpl implements FeedbackService {

  private static final Logger log = LoggerFactory.getLogger(FeedbackServiceImpl.class);
  private final FeedbackRepository feedbackRepository;
  private final EnrollmentService enrollmentService;

  @Inject
  public FeedbackServiceImpl(
      FeedbackRepository feedbackRepository, EnrollmentService enrollmentService) {
    this.feedbackRepository = feedbackRepository;
    this.enrollmentService = enrollmentService;
  }

  /**
   * fetches feedbacks for a given review task ID.
   *
   * @param reviewTaskId the ID of the review task
   * @return a CompletableFuture containing the created FeedbackDTO
   */
  @Override
  public Optional<List<Feedback>> getFeedbacksForReviewTaskId(Long reviewTaskId) {
    return feedbackRepository.findFeedbacksReviewTaskId(reviewTaskId);
  }

  /**
   * Retrieves feedbacks received by a student for a given course code.
   *
   * @param userId the ID of the student
   * @param courseCode the course code
   * @return a CompletableFuture containing a list of FeedbacksReceivedDTO
   */
  @Override
  public CompletableFuture<List<FeedbacksReceivedDTO>> getFeedbacksReceivedByStudent(
      Long userId, String courseCode) {
    return enrollmentService
        .findStudentEnrolledCourseCodes(userId, courseCode)
        .thenCompose(
            courseCodes -> feedbackRepository.findFeedbacksReceivedByStudent(userId, courseCodes))
        .thenApply(
            feedbacks -> {
              Map<Assignment, List<Feedback>> groupedByAssignment =
                  feedbacks.stream()
                      .filter(
                          feedback -> {
                            ReviewTask rt = feedback.getReviewTask();
                            Assignment assignment = rt.getAssignment();
                            return rt.getStatus() == Status.COMPLETED
                                && assignment.getDueDate().isBefore(LocalDate.now());
                          })
                      .collect(
                          Collectors.groupingBy(
                              feedback -> feedback.getReviewTask().getAssignment()));

              List<FeedbacksReceivedDTO> feedbacksReceivedDTOS = new ArrayList<>();
              AtomicInteger peerCounter = new AtomicInteger(1);

              for (Map.Entry<Assignment, List<Feedback>> entry : groupedByAssignment.entrySet()) {
                Assignment assignment = entry.getKey();
                List<Feedback> groupFeedbacks = entry.getValue();

                Map<String, List<Feedback>> groupedByQuestion =
                    groupFeedbacks.stream()
                        .collect(Collectors.groupingBy(f -> f.getQuestion().getQuestionText()));

                int totalMarks =
                    groupedByQuestion.values().stream()
                        .mapToInt(list -> list.get(0).getQuestion().getMaxMarks())
                        .sum();

                // Average score per question
                int marksObtained =
                    groupedByQuestion.values().stream()
                        .mapToInt(
                            list -> {
                              return (int)
                                  list.stream().mapToInt(Feedback::getScore).average().orElse(0);
                            })
                        .sum();

                feedbacksReceivedDTOS.add(
                    new FeedbacksReceivedDTO(
                        assignment.getTitle(),
                        assignment.getAssignmentId(),
                        null,
                        totalMarks,
                        marksObtained,
                        null));
              }
              return feedbacksReceivedDTOS;
            });
  }
}
