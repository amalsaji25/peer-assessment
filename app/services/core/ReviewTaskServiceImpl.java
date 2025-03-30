package services.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import models.Feedback;
import models.ReviewTask;
import models.User;
import models.dto.*;
import models.enums.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.core.ReviewTaskRepository;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReviewTaskServiceImpl implements ReviewTaskService {

    private final ReviewTaskRepository reviewTaskRepository;
    private final FeedbackService feedbackService;
    private static final Logger log = LoggerFactory.getLogger(ReviewTaskServiceImpl.class);

    @Inject
    public ReviewTaskServiceImpl(ReviewTaskRepository reviewTaskRepository, FeedbackService feedbackService) {
        this.reviewTaskRepository = reviewTaskRepository;
        this.feedbackService = feedbackService;
    }


    @Override
    public CompletableFuture<SubmissionOverviewDTO> getReviewTasksSubmissionOverview(Long assignmentId) {
        return getReviewTasks(assignmentId).thenApply(reviewTasks -> {
            if (reviewTasks.isEmpty()) {
                log.info("No ReviewTasks found for assignment {}", assignmentId);
                return null;
            }

            Map<Long, List<ReviewTask>> groupedTasks = groupReviewTasksByGroup(reviewTasks);
            List<GroupSubmissionDTO> groupDTOs = generateSubmissionInfoInEachGroupDTOs(groupedTasks);

            int totalMembers = groupDTOs.stream().mapToInt(GroupSubmissionDTO::getTotalReviewTasks).sum();
            int completedMembers = groupDTOs.stream().mapToInt(GroupSubmissionDTO::getReviewsCompleted).sum();
            int percentageCompleted = (totalMembers == 0) ? 0 : (int) Math.round((completedMembers * 100.0) / totalMembers);

            SubmissionOverviewDTO dto = new SubmissionOverviewDTO();
            dto.setTotalSubmissions(reviewTasks.size());
            dto.setReviewsCompleted(percentageCompleted);
            dto.setGroups(groupDTOs);

            return dto;
        });
    }

    @Override
    public CompletableFuture<List<ReviewTask>> getReviewTasks(Long assignmentId) {
        Optional<List<ReviewTask>> reviewTasks = reviewTaskRepository.findByAssignmentId(assignmentId);
        return CompletableFuture.completedFuture(reviewTasks.orElse(Collections.emptyList()));
    }

    @Override
    public CompletableFuture<Integer> getReviewCountByStatus(Long userId, String courseCode, Status status) {
        if (courseCode == null || courseCode.trim().isEmpty() || courseCode.equalsIgnoreCase("All")) {
            return reviewTaskRepository.findReviewCountByStudentIdAndStatus(userId, status);
        } else {
            return reviewTaskRepository.findReviewCountByStudentIdAndStatusForCourse(userId, courseCode, status);
        }
    }

    @Override
    public CompletableFuture<String> parseAndSaveOrSubmitReviewTask(Long reviewTaskId, JsonNode json) {
        return CompletableFuture.supplyAsync(() -> {
            try{
            Status status = Status.valueOf(json.get("status").asText().toUpperCase());
            ArrayNode feedbacks = (ArrayNode) json.get("feedbacks");

            List<FeedbackDTO> feedbackDTOs = new ArrayList<>();
            for (JsonNode feedback : feedbacks) {
                Long feedbackId = feedback.get("feedbackId").asLong();
                String feedbackText = feedback.has("feedback") ? feedback.get("feedback").asText() : "";
                int score = feedback.get("marks").asInt();
                feedbackDTOs.add(new FeedbackDTO(feedbackId, feedbackText, score));
            }

            ReviewTaskDTO reviewTaskDTO = new ReviewTaskDTO(reviewTaskId, status, feedbackDTOs);

            reviewTaskRepository.saveReviewTaskFeedback(reviewTaskDTO);

            return  "Review " + status.name().toLowerCase() + " successfully";
            }catch (Exception e){
                log.error("Error parsing and saving review task: {}", e.getMessage());
                throw new CompletionException("Failed to parse and save review task" + e.getMessage(), e);
            }
        });
    }



    public Map<Long, List<ReviewTask>> groupReviewTasksByGroup(List<ReviewTask> reviewTasks) {
        return reviewTasks.stream().collect(Collectors.groupingBy(ReviewTask::getGroupId));
    }

    public List<GroupSubmissionDTO> generateSubmissionInfoInEachGroupDTOs(Map<Long, List<ReviewTask>> groupedReviewTasks) {
        List<GroupSubmissionDTO> groupSubmissionDTOList = new ArrayList<>();

        groupedReviewTasks.forEach((groupId, groupReviewTasks) -> {
            GroupSubmissionDTO groupSubmissionDTO = new GroupSubmissionDTO();
            groupSubmissionDTO.setGroupId(groupId);
            groupSubmissionDTO.setGroupName(groupReviewTasks.get(0).getGroupName());

            List<MemberSubmissionDTO> members = generateMemberSubmissionDTOs(groupReviewTasks);

            long completedMembers = members.stream()
                    .filter(m -> "COMPLETED".equalsIgnoreCase(m.getStatus()))
                    .count();

            groupSubmissionDTO.setGroupSize(members.size()); // Total team members
            groupSubmissionDTO.setReviewsCompleted((int) completedMembers); // Members who completed all reviews
            groupSubmissionDTO.setTotalReviewTasks(members.size());
            groupSubmissionDTO.setMembers(members);

            groupSubmissionDTOList.add(groupSubmissionDTO);
        });

        return groupSubmissionDTOList;
    }

    private List<MemberSubmissionDTO> generateMemberSubmissionDTOs(List<ReviewTask> groupReviewTasks) {
        Set<User> groupMembers = groupReviewTasks.stream()
                .map(ReviewTask::getReviewer)
                .collect(Collectors.toSet());

        List<MemberSubmissionDTO> memberSubmissionDTOS = new ArrayList<>();

        groupMembers.forEach(groupMember -> {
            MemberSubmissionDTO dto = new MemberSubmissionDTO();
            dto.setUserId(groupMember.getUserId());
            dto.setUserName(groupMember.getUserName());
            dto.setEmail(groupMember.getEmail());

            List<FeedbackDTO> feedbacks = groupReviewTasks.stream()
                    .filter(task -> task.getReviewee().getUserId().equals(groupMember.getUserId()))
                    .flatMap(task -> {
                        Optional<List<Feedback>> opt = feedbackService.getFeedbacksForReviewTaskId(task.getReviewTaskId());
                        return opt.map(list -> list.stream().map(f -> new FeedbackDTO(
                                f.getScore(),
                                f.getFeedbackText(),
                                f.getQuestion().getQuestionText(),
                                task.getReviewer().getUserId(),
                                task.getReviewer().getUserName()
                        ))).orElse(Stream.empty());
                    })
                    .collect(Collectors.toList());

            dto.setFeedbacks(feedbacks);

            int totalScore = feedbacks.stream().mapToInt(FeedbackDTO::getMaxScore).sum();
            float average = feedbacks.isEmpty() ? 0 : (float) totalScore / feedbacks.size();
            dto.setAverageFeedbackScore(average);

            boolean allSubmitted = groupReviewTasks.stream()
                    .noneMatch(task -> task.getReviewer().getUserId().equals(groupMember.getUserId())
                            && task.getStatus().equals(Status.PENDING));
            dto.setStatus(allSubmitted ? "COMPLETED" : "PENDING");

            memberSubmissionDTOS.add(dto);
        });

        return memberSubmissionDTOS;
    }
}
