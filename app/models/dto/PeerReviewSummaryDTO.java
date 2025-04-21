package models.dto;

/**
 * PeerReviewSummaryDTO is a data transfer object (DTO) that represents the summary of peer reviews
 * for an assignment. It contains fields for the assignment title, course code, completed reviews,
 * total reviews, progress percentage, and total student count.
 */
public class PeerReviewSummaryDTO {
    public String assignmentTitle;
    public String courseCode;
    public int completedReviews;
    public int totalReviews;
    public int progressPercentage;
    public int totalStudentCount;

    public int getTotalStudentCount() {
        return totalStudentCount;
    }

    public void setTotalStudentCount(int totalStudentCount) {
        this.totalStudentCount = totalStudentCount;
    }

    public String getAssignmentTitle() {
        return assignmentTitle;
    }

    public void setAssignmentTitle(String assignmentTitle) {
        this.assignmentTitle = assignmentTitle;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public int getCompletedReviews() {
        return completedReviews;
    }

    public void setCompletedReviews(int completedReviews) {
        this.completedReviews = completedReviews;
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }

    public int getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(int progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

}
