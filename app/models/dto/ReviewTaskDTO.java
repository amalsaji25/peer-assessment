package models.dto;

import models.enums.Status;

import java.time.LocalDate;
import java.util.List;

public class ReviewTaskDTO {
    private Long reviewTaskId;
    private Long assignmentId;
    private LocalDate dueDate;
    private String courseCode;
    private String assignmentName;
    private String revieweeName;
    private Status status;
    private List<FeedbackDTO> feedbacks;

    public ReviewTaskDTO() {}

    public ReviewTaskDTO(Long reviewTaskId, Long assignmentId, LocalDate dueDate, String courseCode, String assignmentName, String revieweeName, Status status, List<FeedbackDTO> feedbacks) {
        this.reviewTaskId = reviewTaskId;
        this.assignmentId = assignmentId;
        this.dueDate = dueDate;
        this.courseCode = courseCode;
        this.assignmentName = assignmentName;
        this.revieweeName = revieweeName;
        this.status = status;
        this.feedbacks = feedbacks;
    }

    public ReviewTaskDTO(Long reviewTaskId, Status status, List<FeedbackDTO> feedbacks) {
        this.reviewTaskId = reviewTaskId;
        this.status = status;
        this.feedbacks = feedbacks;
    }

    public Long getReviewTaskId() {
        return reviewTaskId;
    }

    public void setReviewTaskId(Long reviewTaskId) {
        this.reviewTaskId = reviewTaskId;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getAssignmentName() {
        return assignmentName;
    }

    public void setAssignmentName(String assignmentName) {
        this.assignmentName = assignmentName;
    }

    public String getRevieweeName() {
        return revieweeName;
    }

    public void setRevieweeName(String revieweeName) {
        this.revieweeName = revieweeName;
    }

    public List<FeedbackDTO> getFeedbacks() {
        return feedbacks;
    }

    public void setFeedbacks(List<FeedbackDTO> feedbacks) {
        this.feedbacks = feedbacks;
    }

    public Status getReviewStatus() {
        return status;
    }

    public void setReviewStatus(Status status) {
        this.status = status;
    }
}
