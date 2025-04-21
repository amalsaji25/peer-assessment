package models.dto;

import java.util.List;

/**
 * SubmissionOverviewDTO is a data transfer object (DTO) that represents the overview of submissions
 * for an assignment. It contains fields for the total number of submissions, the number of reviews
 * completed, and a list of group submissions.
 */
public class SubmissionOverviewDTO {
  public int totalSubmissions;
  public int reviewsCompleted;
  public List<GroupSubmissionDTO> groups;

  public int getTotalSubmissions() {
    return totalSubmissions;
  }

  public void setTotalSubmissions(int totalSubmissions) {
    this.totalSubmissions = totalSubmissions;
  }

  public int getReviewsCompleted() {
    return reviewsCompleted;
  }

  public void setReviewsCompleted(int reviewsCompleted) {
    this.reviewsCompleted = reviewsCompleted;
  }

  public List<GroupSubmissionDTO> getGroups() {
    return groups;
  }

  public void setGroups(List<GroupSubmissionDTO> groups) {
    this.groups = groups;
  }
}
