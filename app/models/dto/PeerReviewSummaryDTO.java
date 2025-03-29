package models.dto;

public class PeerReviewSummaryDTO {
    public String assignmentTitle;
    public String courseCode;
    public int completedReviews;
    public int totalReviews;

    public int getTotalStudentCount() {
        return totalStudentCount;
    }

    public void setTotalStudentCount(int totalStudentCount) {
        this.totalStudentCount = totalStudentCount;
    }

    public int progressPercentage;
    public int totalStudentCount;

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
