package models.dto;

import models.Feedback;

import java.util.List;

public class MemberSubmissionDTO {
    public Long userId;
    public String userName;
    public String email;
    public float averageFeedbackScore;
    public String status;
    public List<FeedbackDTO> feedbacks;

    public List<FeedbackDTO> getFeedbacks() {
        return feedbacks;
    }

    public void setFeedbacks(List<FeedbackDTO> feedbacks) {
        this.feedbacks = feedbacks;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public float getAverageFeedbackScore() {
        return averageFeedbackScore;
    }

    public void setAverageFeedbackScore(float averageFeedbackScore) {
        this.averageFeedbackScore = averageFeedbackScore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
