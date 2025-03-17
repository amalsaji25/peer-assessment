package models;


import jakarta.persistence.*;
import models.enums.ReviewStatus;

import java.io.Serializable;

@Entity
@Table(name = "review_tasks")
public class ReviewTasks implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_task_id")
    private Long reviewTaskId;

    @ManyToOne
    @JoinColumn(name = "assignment_id", referencedColumnName = "assignment_id", nullable = false)
    private Assignments assignment;  // The assignment this review belongs to

    @ManyToOne
    @JoinColumn(name = "reviewer_id", referencedColumnName = "user_id", nullable = false)
    private Users reviewer;  // Student doing the review

    @ManyToOne
    @JoinColumn(name = "reviewee_id", referencedColumnName = "user_id", nullable = false)
    private Users reviewee;  // Student being reviewed

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReviewStatus status = ReviewStatus.PENDING;  // Default: Pending

    public ReviewTasks() {}

    public ReviewTasks(Assignments assignment, Users reviewer, Users reviewee, ReviewStatus status) {
        this.assignment = assignment;
        this.reviewer = reviewer;
        this.reviewee = reviewee;
        this.status = status;
    }

    public Long getReviewTaskId() {
        return reviewTaskId;
    }

    public Assignments getAssignment() {
        return assignment;
    }

    public Users getReviewer() {
        return reviewer;
    }

    public Users getReviewee() {
        return reviewee;
    }

    public ReviewStatus getStatus() {
        return status;
    }
}
