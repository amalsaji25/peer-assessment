package models;

import jakarta.persistence.*;
import repository.CourseRepository;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "assignments")
public class Assignments implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Long assignmentId;

    @ManyToOne
    @JoinColumn(name = "course_code", referencedColumnName = "course_code", nullable = false)
    private Courses course;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Column(name = "number_of_reviews_required", nullable = false)
    private int numReviewsRequired;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL)
    private List<ReviewTasks> reviewTasks;

    public Assignments() {}

    public Assignments(String courseCode, String title, String description, LocalDateTime dueDate, int numReviewsRequired, CourseRepository courseRepository) {
        this.course = courseRepository.findByCourseCode(courseCode).orElseThrow(() -> new IllegalArgumentException("Course not found with code " + courseCode));
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.numReviewsRequired = numReviewsRequired;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public Courses getCourse() {
        return course;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public int getNumReviewsRequired() {
        return numReviewsRequired;
    }

    public List<ReviewTasks> getReviewTasks() {
        return reviewTasks;
    }
}
