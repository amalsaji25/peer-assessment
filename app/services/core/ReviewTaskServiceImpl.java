package services.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import models.Feedback;
import models.ReviewTask;
import models.User;
import models.dto.*;
import models.enums.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.core.ReviewTaskRepository;

/**
 * ReviewTaskServiceImpl is a service class that implements the ReviewTaskService interface. It
 * provides methods to interact with the ReviewTaskRepository for managing review tasks in the
 * system.
 */
public class ReviewTaskServiceImpl implements ReviewTaskService {

  private static final Logger log = LoggerFactory.getLogger(ReviewTaskServiceImpl.class);
  private final ReviewTaskRepository reviewTaskRepository;
  private final FeedbackService feedbackService;

  @Inject
  public ReviewTaskServiceImpl(
      ReviewTaskRepository reviewTaskRepository, FeedbackService feedbackService) {
    this.reviewTaskRepository = reviewTaskRepository;
    this.feedbackService = feedbackService;
  }

  /**
   * Retrieves the submission overview for review tasks of a given assignment.
   *
   * @param assignmentId the ID of the assignment
   * @return a CompletableFuture containing the SubmissionOverviewDTO
   */
  @Override
  public CompletableFuture<SubmissionOverviewDTO> getReviewTasksSubmissionOverview(
      Long assignmentId) {
    return getReviewTasks(assignmentId)
        .thenApply(
            reviewTasks -> {
              if (reviewTasks.isEmpty()) {
                log.info("No ReviewTasks found for assignment {}", assignmentId);
                return null;
              }

              Map<Long, List<ReviewTask>> groupedTasks = groupReviewTasksByGroup(reviewTasks);
              List<GroupSubmissionDTO> groupDTOs =
                  generateSubmissionInfoInEachGroupDTOs(groupedTasks);
              calculateClassAverages(groupDTOs);

              int totalMembers =
                  groupDTOs.stream().mapToInt(GroupSubmissionDTO::getTotalReviewTasks).sum();
              int completedMembers =
                  groupDTOs.stream().mapToInt(GroupSubmissionDTO::getReviewsCompleted).sum();
              int percentageCompleted =
                  (totalMembers == 0)
                      ? 0
                      : (int) Math.round((completedMembers * 100.0) / totalMembers);

              int totalStudents =
                  groupDTOs.stream().mapToInt(GroupSubmissionDTO::getGroupSize).sum();

              SubmissionOverviewDTO dto = new SubmissionOverviewDTO();
              dto.setTotalSubmissions(totalStudents);
              dto.setReviewsCompleted(percentageCompleted);
              dto.setGroups(groupDTOs);

              return dto;
            });
  }

  /**
   * Calculates class averages for each question and overall average for all groups.
   *
   * @param groupDTOs the list of GroupSubmissionDTOs
   */
  @Override
  public void calculateClassAverages(List<GroupSubmissionDTO> groupDTOs) {

    // Step 1: Collect all members from all groups
    List<MemberSubmissionDTO> members =
        groupDTOs.stream().flatMap(g -> g.getMembers().stream()).toList();

    // calculate class average for each question
    Map<String, List<Float>> questionAverages = new LinkedHashMap<>();
    for (MemberSubmissionDTO member : members) {
      for (MemberSubmissionDTO.EvaluationMatrixDTO matrix : member.getEvaluationMatrix()) {
        String question = matrix.getFeedbackQuestion();
        float avg = matrix.getAverageMarkForQuestion();

        questionAverages.computeIfAbsent(question, k -> new ArrayList<>()).add(avg);
      }
    }

    // Calculate the average for each question
    Map<String, Float> classAveragesForEachQuestion = new LinkedHashMap<>();
    for (Map.Entry<String, List<Float>> entry : questionAverages.entrySet()) {
      String question = entry.getKey();
      List<Float> averages = entry.getValue();

      float avg = (float) averages.stream().mapToDouble(Float::doubleValue).average().orElse(0);
      classAveragesForEachQuestion.put(question, avg);
    }

    float overallClassAverage =
        (float)
            classAveragesForEachQuestion.values().stream().mapToDouble(Float::doubleValue).sum();

    groupDTOs.stream()
        .flatMap(g -> g.getMembers().stream())
        .forEach(
            member -> {
              member.setClassAveragesForEachQuestion(classAveragesForEachQuestion);
              member.setOverallClassAverage(overallClassAverage);
            });
  }

  /**
   * Retrieves review tasks for a given assignment ID.
   *
   * @param assignmentId the ID of the assignment
   * @return a CompletableFuture containing a list of ReviewTask
   */
  @Override
  public CompletableFuture<List<ReviewTask>> getReviewTasks(Long assignmentId) {
    Optional<List<ReviewTask>> reviewTasks = reviewTaskRepository.findByAssignmentId(assignmentId);
    return CompletableFuture.completedFuture(reviewTasks.orElse(Collections.emptyList()));
  }

  /**
   * Retrieves the count of review tasks by status for a given user ID and course code.
   *
   * @param userId the ID of the user
   * @param courseCode the course code
   * @param status the status of the review task
   * @return a CompletableFuture containing the count of review tasks
   */
  @Override
  public CompletableFuture<Integer> getReviewCountByStatus(
      Long userId, String courseCode, Status status) {
    if (courseCode == null || courseCode.trim().isEmpty() || courseCode.equalsIgnoreCase("All")) {
      return reviewTaskRepository.findReviewCountByStudentIdAndStatus(userId, status);
    } else {
      return reviewTaskRepository.findReviewCountByStudentIdAndStatusForCourse(
          userId, courseCode, status);
    }
  }

  /**
   * Parses the JSON data and saves or submits the review task.
   *
   * @param reviewTaskId the ID of the review task
   * @param json the JSON data containing the review task details
   * @return a CompletableFuture containing the result of the operation
   */
  @Override
  public CompletableFuture<String> parseAndSaveOrSubmitReviewTask(
      Long reviewTaskId, JsonNode json) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            Status status = Status.valueOf(json.get("status").asText().toUpperCase());
            ArrayNode feedbacks = (ArrayNode) json.get("feedbacks");

            List<FeedbackDTO> feedbackDTOs = new ArrayList<>();
            for (JsonNode feedback : feedbacks) {
              Long feedbackId = feedback.get("feedbackId").asLong();
              String feedbackText =
                  feedback.has("feedback") ? feedback.get("feedback").asText() : "";
              int score = feedback.get("marks").asInt();
              feedbackDTOs.add(new FeedbackDTO(feedbackId, feedbackText, score));
            }

            ReviewTaskDTO reviewTaskDTO = new ReviewTaskDTO(reviewTaskId, status, feedbackDTOs);

            reviewTaskRepository.saveReviewTaskFeedback(reviewTaskDTO);

            return "Review " + status.name().toLowerCase() + " successfully";
          } catch (Exception e) {
            log.error("Error parsing and saving review task: {}", e.getMessage());
            throw new CompletionException(
                "Failed to parse and save review task" + e.getMessage(), e);
          }
        });
  }

  /**
   * Groups review tasks by their group ID.
   *
   * @param reviewTasks the list of review tasks to be grouped
   * @return a map where the key is the group ID and the value is a list of review tasks
   */
  @Override
  public Map<Long, List<ReviewTask>> groupReviewTasksByGroup(List<ReviewTask> reviewTasks) {
    return reviewTasks.stream().collect(Collectors.groupingBy(ReviewTask::getGroupId));
  }

  /**
   * Generates a list of GroupSubmissionDTOs from the grouped review tasks.
   *
   * @param groupedReviewTasks the map of grouped review tasks
   * @return a list of GroupSubmissionDTOs
   */
  @Override
  public List<GroupSubmissionDTO> generateSubmissionInfoInEachGroupDTOs(
      Map<Long, List<ReviewTask>> groupedReviewTasks) {
    List<GroupSubmissionDTO> groupSubmissionDTOList = new ArrayList<>();

    groupedReviewTasks.forEach(
        (groupId, groupReviewTasks) -> {
          GroupSubmissionDTO groupSubmissionDTO = new GroupSubmissionDTO();
          groupSubmissionDTO.setGroupId(groupId);
          groupSubmissionDTO.setGroupName(groupReviewTasks.get(0).getGroupName());

          List<MemberSubmissionDTO> members = generateMemberSubmissionDTOs(groupReviewTasks);

          long completedMembers =
              members.stream().filter(m -> "COMPLETED".equalsIgnoreCase(m.getStatus())).count();

          groupSubmissionDTO.setGroupSize(members.size()); // Total team members
          groupSubmissionDTO.setReviewsCompleted(
              (int) completedMembers); // Members who completed all reviews
          groupSubmissionDTO.setTotalReviewTasks(members.size());
          groupSubmissionDTO.setMembers(members);

          List<FeedbackDTO> privateComments =
              groupReviewTasks.stream()
                  .flatMap(
                      task -> {
                        Optional<List<Feedback>> opt =
                            feedbackService.getFeedbacksForReviewTaskId(task.getReviewTaskId());
                        return opt.map(List::stream).orElse(Stream.empty());
                      })
                  .filter(
                      f ->
                          f.getQuestion()
                              .getQuestionText()
                              .equalsIgnoreCase("Private Comment for Professor"))
                  .map(
                      f ->
                          new FeedbackDTO(
                              f.getId(),
                              f.getScore(),
                              f.getQuestion().getMaxMarks(),
                              f.getFeedbackText(),
                              f.getQuestion().getQuestionText(),
                              f.getReviewTask().getReviewer().getUserId(),
                              f.getReviewTask().getReviewer().getUserName()))
                  .collect(Collectors.toList());

          groupSubmissionDTO.setPrivateComments(privateComments);

          groupSubmissionDTOList.add(groupSubmissionDTO);
        });

    return groupSubmissionDTOList;
  }

  /**
   * Generates a list of MemberSubmissionDTOs from the review tasks of a group.
   *
   * @param groupReviewTasks the list of review tasks for a group
   * @return a list of MemberSubmissionDTOs
   */
  private List<MemberSubmissionDTO> generateMemberSubmissionDTOs(
      List<ReviewTask> groupReviewTasks) {
    // Get members of the group
    Set<User> groupMembers =
        groupReviewTasks.stream().map(ReviewTask::getReviewer).collect(Collectors.toSet());

    List<MemberSubmissionDTO> memberSubmissionDTOS = new ArrayList<>();

    // Process each group member
    groupMembers.forEach(
        groupMember -> {
          MemberSubmissionDTO dto = new MemberSubmissionDTO();
          dto.setUserId(groupMember.getUserId());
          dto.setUserName(groupMember.getUserName());
          dto.setEmail(groupMember.getEmail());

          // 1. Group feedbacks by reviewers for this reviewee (current group member being
          // processed)
          Map<Long, List<FeedbackDTO>> feedbacksByReviewer =
              groupReviewTasks.stream()
                  .filter(task -> task.getReviewee().getUserId().equals(groupMember.getUserId()))
                  .flatMap(
                      task -> {
                        Optional<List<Feedback>> opt =
                            feedbackService.getFeedbacksForReviewTaskId(task.getReviewTaskId());
                        return opt.map(
                                list ->
                                    list.stream()
                                        .map(
                                            f ->
                                                new FeedbackDTO(
                                                    f.getId(),
                                                    f.getScore(),
                                                    f.getQuestion().getMaxMarks(),
                                                    f.getFeedbackText(),
                                                    f.getQuestion().getQuestionText(),
                                                    task.getReviewer().getUserId(),
                                                    task.getReviewer().getUserName())))
                            .orElse(Stream.empty());
                      })
                  .collect(Collectors.groupingBy(FeedbackDTO::getReviewerId));

          dto.setFeedbacks(feedbacksByReviewer);

          // 2. Flatten all feedbacks for this reviewee into a single list
          List<FeedbackDTO> allFeedbacks =
              feedbacksByReviewer.values().stream().flatMap(List::stream).toList();

          // 3. Get reviewer names
          List<String> reviewerNames =
              allFeedbacks.stream()
                  .map(FeedbackDTO::getReviewerName)
                  .distinct()
                  .sorted()
                  .collect(Collectors.toList());

          dto.setReviewerNames(reviewerNames);

          // 3a. Count how many reviewers actually submitted their review for this student
          long respondedCount =
              groupReviewTasks.stream()
                  .filter(
                      task ->
                          task.getReviewee().getUserId().equals(groupMember.getUserId())
                              && task.getStatus() == Status.COMPLETED)
                  .count();
          dto.setReviewersResponseCount((int) respondedCount);

          // 4. Build Evaluation Matrix (Grouping feedbacks by question text )
          Map<String, List<FeedbackDTO>> groupedByQuestion =
              allFeedbacks.stream()
                  .filter(
                      f -> !f.getQuestionText().equalsIgnoreCase("Private Comment for Professor"))
                  .filter(f -> !f.getQuestionText().equalsIgnoreCase("Overall Feedback Comment"))
                  .collect(
                      Collectors.groupingBy(
                          FeedbackDTO::getQuestionText, LinkedHashMap::new, Collectors.toList()));

          // 4a. For each question, builds a row of marks per reviewer. If a reviewer didn’t answer
          // that question, a zero is filled in.
          List<MemberSubmissionDTO.EvaluationMatrixDTO> matrix = new ArrayList<>();
          for (Map.Entry<String, List<FeedbackDTO>> entry : groupedByQuestion.entrySet()) {
            String question = entry.getKey();
            List<FeedbackDTO> feedbacks = entry.getValue();

            List<Integer> marksPerReviewer = new ArrayList<>();
            for (String reviewer : reviewerNames) {
              feedbacks.stream()
                  .filter(f -> f.getReviewerName().equals(reviewer))
                  .findFirst()
                  .ifPresentOrElse(
                      f -> marksPerReviewer.add(f.getObtainedScore()),
                      () -> marksPerReviewer.add(0));
            }

            // 4.b  Calculate average for this question based on the marks all reviewers gave
            float avg =
                (float) marksPerReviewer.stream().mapToInt(Integer::intValue).average().orElse(0);
            matrix.add(
                new MemberSubmissionDTO.EvaluationMatrixDTO(question, marksPerReviewer, avg));
          }
          dto.setEvaluationMatrix(matrix);

          // 5. compute average of the feedback scores a reviewer gave across all feedback questions
          // for a specific reviewee (each column in the matrix corresponds to a reviewer)
          List<Float> reviewerAverages = new ArrayList<>();
          for (int i = 0; i < reviewerNames.size(); i++) {
            final int index = i;
            float colSum =
                (float) matrix.stream().mapToInt(row -> row.getMarksPerReviewer().get(index)).sum();
            reviewerAverages.add(colSum);
          }
          dto.setReviewerAverages(reviewerAverages);

          // 6. Overall average feedback score from all reviewers for this reviewee
          if (matrix.isEmpty()) {
            dto.setAverageFeedbackScore(0);
          } else {

            float overallAverage =
                (float)
                    reviewerAverages.stream().mapToDouble(Float::doubleValue).average().orElse(0.0);
            dto.setAverageFeedbackScore(overallAverage);
          }

          // 7. Peer comments (Overall Comment)
          List<FeedbackDTO> peerComments =
              allFeedbacks.stream()
                  .filter(f -> f.getQuestionText().equalsIgnoreCase("Overall Comment"))
                  .collect(Collectors.toList());
          dto.setFeedbacksPerQuestion(peerComments);

          // 8. Private comments
          List<FeedbackDTO> privateComments =
              allFeedbacks.stream()
                  .filter(
                      f -> f.getQuestionText().equalsIgnoreCase("Private Comment for Professor"))
                  .collect(Collectors.toList());
          dto.setPrivateComments(privateComments);

          int totalScore =
              feedbacksByReviewer.values().stream()
                  .flatMap(List::stream)
                  .mapToInt(FeedbackDTO::getMaxScore)
                  .sum();
          float average =
              feedbacksByReviewer.isEmpty() ? 0 : (float) totalScore / feedbacksByReviewer.size();
          dto.setMaximumAverageFeedbackScoreForReviewTask(average);

          boolean allSubmitted =
              groupReviewTasks.stream()
                  .noneMatch(
                      task ->
                          task.getReviewer().getUserId().equals(groupMember.getUserId())
                              && task.getStatus().equals(Status.PENDING));
          dto.setStatus(allSubmitted ? "COMPLETED" : "PENDING");

          memberSubmissionDTOS.add(dto);
        });

    return memberSubmissionDTOS;
  }
}
