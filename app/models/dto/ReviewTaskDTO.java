package models.dto;

import java.time.LocalDate;
import java.util.List;
import models.enums.Status;

/**
 * ReviewTaskDTO is a data transfer object (DTO) that represents the data for a review task,
 * including information about the assignment, due date, course code, assignment name, reviewee
 * name, status, and feedbacks.
 */
public class ReviewTaskDTO {
  private Long reviewTaskId;
  private Long assignmentId;
  private LocalDate dueDate;
  private String courseCode;
  private String assignmentName;
  private String revieweeName;
  private Status status;
  private Boolean reviewTaskForProfessor;
  private List<FeedbackDTO> feedbacks;

  public ReviewTaskDTO() {}

  public ReviewTaskDTO(
      Long reviewTaskId,
      Long assignmentId,
      LocalDate dueDate,
      String courseCode,
      String assignmentName,
      String revieweeName,
      Status status,
      List<FeedbackDTO> feedbacks,
      Boolean reviewTaskForProfessor) {
    this.reviewTaskId = reviewTaskId;
    this.assignmentId = assignmentId;
    this.dueDate = dueDate;
    this.courseCode = courseCode;
    this.assignmentName = assignmentName;
    this.revieweeName = revieweeName;
    this.status = status;
    this.feedbacks = feedbacks;
    this.reviewTaskForProfessor = reviewTaskForProfessor;
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

  public Boolean isReviewTaskForProfessor() {
    return reviewTaskForProfessor;
  }

  public void setReviewTaskForProfessor(Boolean reviewTaskForProfessor) {
    this.reviewTaskForProfessor = reviewTaskForProfessor;
  }
}
