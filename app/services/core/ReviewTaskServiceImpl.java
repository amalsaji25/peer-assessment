package services.core;

import models.Feedback;
import models.ReviewTask;
import models.User;
import models.dto.FeedbackDTO;
import models.dto.MemberSubmissionDTO;
import models.dto.GroupSubmissionDTO;
import models.dto.SubmissionOverviewDTO;
import models.enums.ReviewStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.core.ReviewTaskRepository;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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

            int completed = countCompletedTasks(reviewTasks);
            Map<Long, List<ReviewTask>> groupedTasks = groupReviewTasksByGroup(reviewTasks);
            List<GroupSubmissionDTO> groupDTOs = generateSubmissionInfoInEachGroupDTOs(groupedTasks);

            SubmissionOverviewDTO dto = new SubmissionOverviewDTO();
            dto.setTotalSubmissions(reviewTasks.size());
            dto.setReviewsCompleted(completed);
            dto.setGroups(groupDTOs);

            return dto;
        });
    }

    @Override
    public CompletableFuture<List<ReviewTask>> getReviewTasks(Long assignmentId) {
        Optional<List<ReviewTask>> reviewTasks = reviewTaskRepository.findByAssignmentId(assignmentId);
        return CompletableFuture.completedFuture(reviewTasks.orElse(Collections.emptyList()));
    }

    private int countCompletedTasks(List<ReviewTask> reviewTasks) {
        return (int) reviewTasks.stream()
                .filter(task -> task.getStatus().equals(ReviewStatus.COMPLETED))
                .count();
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
            groupSubmissionDTO.setGroupSize(groupReviewTasks.size());
            groupSubmissionDTO.setReviewsCompleted(countCompletedTasks(groupReviewTasks));
            groupSubmissionDTO.setTotalReviewTasks(groupReviewTasks.size());

            groupSubmissionDTO.setMembers(generateMemberSubmissionDTOs(groupReviewTasks));
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

            int totalScore = feedbacks.stream().mapToInt(FeedbackDTO::getScore).sum();
            float average = feedbacks.isEmpty() ? 0 : (float) totalScore / feedbacks.size();
            dto.setAverageFeedbackScore(average);

            boolean allSubmitted = groupReviewTasks.stream()
                    .noneMatch(task -> task.getReviewer().getUserId().equals(groupMember.getUserId())
                            && task.getStatus().equals(ReviewStatus.PENDING));
            dto.setStatus(allSubmitted ? "COMPLETED" : "PENDING");

            memberSubmissionDTOS.add(dto);
        });

        return memberSubmissionDTOS;
    }
}
