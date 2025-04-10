package services.report;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import models.ReviewTask;
import models.dto.FeedbackDTO;
import models.dto.GroupSubmissionDTO;
import models.dto.MemberSubmissionDTO;
import models.dto.ReportDTO;
import services.core.ReviewTaskService;

/**
 * StudentReportServiceImpl is a service class that implements the ReportService interface. It
 * provides methods to generate reports for students based on their review tasks and feedbacks.
 */
public class StudentReportServiceImpl implements ReportService {

  private final ReviewTaskService reviewTaskService;

  @Inject
  public StudentReportServiceImpl(ReviewTaskService reviewTaskService) {
    this.reviewTaskService = reviewTaskService;
  }

  /**
   * Generates a report for a student based on the assignment ID and user ID.
   *
   * @param AssignmentId the ID of the assignment
   * @param userId the ID of the student
   * @return a CompletableFuture containing the generated ReportDTO
   */
  @Override
  public CompletableFuture<ReportDTO> generateReport(Long AssignmentId, Long userId) {
    return reviewTaskService
        .getReviewTasks(AssignmentId)
        .thenApply(
            reviewTasks -> {
              ReportDTO reportDTO = new ReportDTO();

              reportDTO.setAssignmentId(AssignmentId);
              reportDTO.setAssignmentTitle(reviewTasks.get(0).getAssignment().getTitle());
              reportDTO.setCourseName(
                  reviewTasks.get(0).getAssignment().getCourse().getCourseName());
              reportDTO.setCourseCode(
                  reviewTasks.get(0).getAssignment().getCourse().getCourseCode());
              reportDTO.setCourseSection(
                  reviewTasks.get(0).getAssignment().getCourse().getCourseSection());
              reportDTO.setTerm(reviewTasks.get(0).getAssignment().getCourse().getTerm());

              Map<Long, List<ReviewTask>> groupedTasks =
                  reviewTaskService.groupReviewTasksByGroup(reviewTasks);
              List<GroupSubmissionDTO> groupDTOs =
                  reviewTaskService.generateSubmissionInfoInEachGroupDTOs(groupedTasks);
              reviewTaskService.calculateClassAverages(groupDTOs);

              // find the group of the user
              GroupSubmissionDTO userGroup =
                  groupDTOs.stream()
                      .filter(
                          group ->
                              group.getMembers().stream()
                                  .anyMatch(member -> member.getUserId().equals(userId)))
                      .findFirst()
                      .orElseThrow(() -> new RuntimeException("Student not found in any group"));

              MemberSubmissionDTO user =
                  userGroup.getMembers().stream()
                      .filter(member -> member.getUserId().equals(userId))
                      .findFirst()
                      .orElseThrow(() -> new RuntimeException("Student not found in the group"));

              reportDTO.setStudentId(userId);
              reportDTO.setStudentName(user.getUserName());
              reportDTO.setStudentGroupName(userGroup.getGroupName());
              reportDTO.setClassAverage(user.getOverallClassAverage());

              // Anonymize reviewers
              Map<Long, String> reviewerNameMap = new HashMap<>();
              AtomicInteger peerIndex = new AtomicInteger(1);

              for (Map.Entry<Long, List<FeedbackDTO>> entry :
                  user.getFeedbacksByReviewer().entrySet()) {
                Long reviewerId = entry.getKey();
                String peerName =
                    reviewerNameMap.computeIfAbsent(
                        reviewerId, id -> "Peer " + peerIndex.getAndIncrement());
                for (FeedbackDTO feedback : entry.getValue()) {
                  feedback.setReviewerName(peerName);
                }
              }

              // Set the feedbacks for the user
              reportDTO.setGroupedAnonymizedFeedbacks(user.getFeedbacksByReviewer());
              reportDTO.setClassAveragePerQuestion(user.getClassAverages());
              reportDTO.setStudentAverageScore(user.getAverageFeedbackScore());
              return reportDTO;
            });
  }
}
