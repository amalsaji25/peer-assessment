package models.dto;

import java.util.List;

/**
 * FeedbacksReceivedDTO is a data transfer object (DTO) that represents the feedback received for an
 * assignment. It contains fields for the assignment title, assignment ID, peer label, total marks,
 * obtained marks, and a list of feedbacks.
 */
public class FeedbacksReceivedDTO {
  private String assignmentTitle;
  private Long assignmentId;
  private String peerLabel;
  private int totalMarks;
  private int obtainedMarks;
  private List<FeedbackDTO> feedbacks;

  public FeedbacksReceivedDTO() {}

  public FeedbacksReceivedDTO(
      String assignmentTitle,
      Long assignmentId,
      String peerLabel,
      int totalMarks,
      int obtainedMarks,
      List<FeedbackDTO> feedbacks) {
    this.assignmentTitle = assignmentTitle;
    this.assignmentId = assignmentId;
    this.peerLabel = peerLabel;
    this.totalMarks = totalMarks;
    this.obtainedMarks = obtainedMarks;
    this.feedbacks = feedbacks;
  }

  public String getAssignmentTitle() {
    return assignmentTitle;
  }

  public void setAssignmentTitle(String assignmentTitle) {
    this.assignmentTitle = assignmentTitle;
  }

  public int getTotalMarks() {
    return totalMarks;
  }

  public void setTotalMarks(int totalMarks) {
    this.totalMarks = totalMarks;
  }

  public int getObtainedMarks() {
    return obtainedMarks;
  }

  public void setObtainedMarks(int obtainedMarks) {
    this.obtainedMarks = obtainedMarks;
  }

  public List<FeedbackDTO> getFeedbacks() {
    return feedbacks;
  }

  public void setFeedbacks(List<FeedbackDTO> feedbacks) {
    this.feedbacks = feedbacks;
  }

  public String getPeerLabel() {
    return peerLabel;
  }

  public void setPeerLabel(String peerLabel) {
    this.peerLabel = peerLabel;
  }

  public Long getAssignmentId() {
    return assignmentId;
  }

  public void setAssignmentId(Long assignmentId) {
    this.assignmentId = assignmentId;
  }
}
