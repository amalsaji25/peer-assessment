package services.report;

import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import models.ReviewTask;
import models.dto.GroupSubmissionDTO;
import models.dto.ReportDTO;
import services.core.ReviewTaskService;

/**
 * ProfessorReportServiceImpl is a service class that implements the ReportService interface. It
 * provides a method to generate a report for a given assignment ID and user ID.
 */
public class ProfessorReportServiceImpl implements ReportService {

  private final ReviewTaskService reviewTaskService;

  @Inject
  public ProfessorReportServiceImpl(ReviewTaskService reviewTaskService) {
    this.reviewTaskService = reviewTaskService;
  }

  /**
   * Generates a report for a professor based on the assignment ID and user ID.
   *
   * @param assignmentId the ID of the assignment
   * @param userId the ID of the professor
   * @return a CompletableFuture containing the generated ReportDTO
   */
  @Override
  public CompletableFuture<ReportDTO> generateReport(Long assignmentId, Long userId) {
    return reviewTaskService
        .getReviewTasks(assignmentId)
        .thenApply(
            reviewTasks -> {
              ReportDTO reportDTO = new ReportDTO();

              reportDTO.setAssignmentId(assignmentId);
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

              int totalEvaluations =
                  groupDTOs.stream().mapToInt(group -> group.getMembers().size()).sum();
              int completedEvaluations =
                  groupDTOs.stream().mapToInt(GroupSubmissionDTO::getReviewsCompleted).sum();
              int incompleteEvaluations = totalEvaluations - completedEvaluations;

              reportDTO.setTotalTeams(groupDTOs.size());
              reportDTO.setTotalEvaluations(totalEvaluations);
              reportDTO.setCompletedEvaluations(completedEvaluations);
              reportDTO.setIncompleteEvaluations(incompleteEvaluations);

              reportDTO.setGroups(groupDTOs);

              return reportDTO;
            });
  }
}
