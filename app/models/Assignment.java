package models;

import jakarta.persistence.*;
import repository.core.CourseRepository;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "assignments")
public class Assignment implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Long assignmentId;

    @ManyToOne
    @JoinColumn(name = "course_code", referencedColumnName = "course_code", nullable = false)
    private Course course;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewTask> reviewTasks;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedbackQuestion> feedbackQuestions;

    public Assignment() {}

    public Assignment(String courseCode, String title, String description, LocalDate startDate, LocalDate dueDate, CourseRepository courseRepository) {
        this.course = courseRepository.findByCourseCode(courseCode).orElseThrow(() -> new IllegalArgumentException("Course not found with code " + courseCode));
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.dueDate = dueDate;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public Course getCourse() {
        return course;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public List<ReviewTask> getReviewTasks() {
        return reviewTasks;
    }

    public List<FeedbackQuestion> getFeedbackQuestions() {
        return feedbackQuestions;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void setReviewTasks(List<ReviewTask> reviewTasks) {
        this.reviewTasks = reviewTasks;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setFeedbackQuestions(List<FeedbackQuestion> feedbackQuestions) {
        this.feedbackQuestions = feedbackQuestions;
    }
}
