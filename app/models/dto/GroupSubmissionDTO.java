package models.dto;

import java.util.List;

/**
 * GroupSubmissionDTO is a data transfer object (DTO) that represents the submission details of a
 * group in an assignment. It contains fields for the group ID, group name, group size, number of
 * reviews completed, total review tasks, private comments, and a list of members in the group.
 */
public class GroupSubmissionDTO {
    public Long groupId;
    public String groupName;
    public int groupSize;
    public int reviewsCompleted;
    public int totalReviewTasks;
    public List<MemberSubmissionDTO> members;
    private List<FeedbackDTO> privateComments;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getGroupSize() {
        return groupSize;
    }

    public void setGroupSize(int groupSize) {
        this.groupSize = groupSize;
    }

    public int getReviewsCompleted() {
        return reviewsCompleted;
    }

    public void setReviewsCompleted(int reviewsCompleted) {
        this.reviewsCompleted = reviewsCompleted;
    }

    public List<MemberSubmissionDTO> getMembers() {
        return members;
    }

    public void setMembers(List<MemberSubmissionDTO> members) {
        this.members = members;
    }

    public int getTotalReviewTasks() {
        return totalReviewTasks;
    }

    public void setTotalReviewTasks(int totalReviewTasks) {
        this.totalReviewTasks = totalReviewTasks;
    }

    public List<FeedbackDTO> getPrivateComments() {
        return privateComments;
    }

    public void setPrivateComments(List<FeedbackDTO> privateComments) {
        this.privateComments = privateComments;
    }
}
