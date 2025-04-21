package forms;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Singleton;

/**
 * AssignmentForm is a data transfer object (DTO) that represents the form data for creating or
 * updating an assignment. It contains fields for the assignment's title, course, course section,
 * course code, term, description, due date, start date, and a list of review questions.
 */
@Singleton
public class AssignmentForm {
    public String title;
    public String course;
    public String courseSection;
    public String courseCode;
    public String term;
    public String description;
    public LocalDate dueDate;
    public LocalDate startDate;
    public List<ReviewQuestionForm> questions;


    public AssignmentForm(){}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public List<ReviewQuestionForm> getQuestions() {
        if (questions == null) {
            questions = new ArrayList<>();
        }
        return questions;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getCourseSection() {
        return courseSection;
    }

    public void setCourseSection(String courseSection) {
        this.courseSection = courseSection;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public static class ReviewQuestionForm {
        public Long questionId;
        public String question;
        public int marks;

        public ReviewQuestionForm(Long questionId, String question, int marks) {
            this.questionId = questionId;
            this.question = question;
            this.marks = marks;
        }
    }
}
