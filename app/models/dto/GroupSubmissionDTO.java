package models.dto;

import java.util.List;

public class GroupSubmissionDTO {
    public Long groupId;
    public String groupName;
    public int groupSize;
    public int reviewsCompleted;
    public int totalReviewTasks;
    public List<MemberSubmissionDTO> members;

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
}
