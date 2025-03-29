package models.dto;

import java.util.List;

public class AssignmentEditDTO {
    public Long assignmentId;
    public String title;
    public String courseCode;
    public String startDate;
    public String dueDate;
    public String description;
    public List<ReviewQuestionDTO> reviewQuestions;

    public static class ReviewQuestionDTO {
        public Long questionId;
        public String question;
        public int marks;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ReviewQuestionDTO> getReviewQuestions() {
        return reviewQuestions;
    }

    public void setReviewQuestions(List<ReviewQuestionDTO> reviewQuestions) {
        this.reviewQuestions = reviewQuestions;
    }
}
