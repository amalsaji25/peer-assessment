package forms;

import javax.inject.Singleton;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class AssignmentForm {
    public String title;
    public String courseCode;
    public String description;
    public LocalDate dueDate;
    public LocalDate startDate;
    public List<ReviewQuestionForm> questions;


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

    public AssignmentForm(){}

    public String getTitle() {
        return title;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDueDate() {
        return dueDate;
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

}
