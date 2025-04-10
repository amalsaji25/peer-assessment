package models.dto;

import java.util.List;

/**
 * AssignmentEditDTO is a data transfer object (DTO) that represents the form data for creating or
 * updating an assignment. It contains fields for the assignment's title, course, course section,
 * course code, term, description, due date, start date, and a list of review questions.
 */
public class AssignmentEditDTO {
    public Long assignmentId;
    public String title;
    public String courseCode;
    public String startDate;
    public String dueDate;
    public String description;
    public String courseSection;
    public String term;
    public List<ReviewQuestionDTO> reviewQuestions;

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

    public String getCourseSection() {
        return courseSection;
    }

    public void setCourseSection(String courseSection) {
        this.courseSection = courseSection;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public static class ReviewQuestionDTO {
        public Long questionId;
        public String question;
        public int marks;
    }
}
