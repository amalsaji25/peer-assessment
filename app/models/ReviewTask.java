package models;


import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import models.enums.Status;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * ReviewTask is an entity class that represents a review task in the system. It contains fields for
 * the review task's ID, the assignment it belongs to, the reviewer and reviewee, the status of the
 * review, and the group information. The class also includes methods for getting and setting these
 * fields.
 */
@Entity
@Table(name = "review_tasks")
public class ReviewTask implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_task_id")
    private Long reviewTaskId;

    @ManyToOne
    @JoinColumn(name = "assignment_id", referencedColumnName = "assignment_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Assignment assignment;  // The assignment this review belongs to

    @ManyToOne
    @JoinColumn(name = "reviewer_id", referencedColumnName = "user_id", nullable = false)
    private User reviewer;  // Student doing the review

    @ManyToOne
    @JoinColumn(name = "reviewee_id", referencedColumnName = "user_id", nullable = false)
    private User reviewee;  // Student being reviewed

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;  // Default: Pending

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "group_name", nullable = false)
    private String groupName;

    @Column(name = "group_size", nullable = false)
    private int groupSize;

    @OneToMany(mappedBy = "reviewTask", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Feedback> feedbacks = new ArrayList<>();

    @Column(name = "review_task_for_professor", nullable = false)
    private boolean reviewTaskForProfessor = false;

    public ReviewTask() {}

    public ReviewTask(Assignment assignment, User reviewer, User reviewee, Status status, Long groupId, String groupName, int groupSize, boolean reviewTaskForProfessor) {
        this.assignment = assignment;
        this.reviewer = reviewer;
        this.reviewee = reviewee;
        this.status = status;
        this.groupId = groupId;
        this.groupName = groupName;
        this.groupSize = groupSize;
        this.reviewTaskForProfessor = reviewTaskForProfessor;
    }

    public Long getReviewTaskId() {
        return reviewTaskId;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public User getReviewer() {
        return reviewer;
    }

    public User getReviewee() {
        return reviewee;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getGroupSize() {
        return groupSize;
    }

    public String getGroupName() {
        return groupName;
    }

    public Long getGroupId() {
        return groupId;
    }

    public List<Feedback> getFeedbacks() {
        return feedbacks;
    }

    public void setFeedbacks(List<Feedback> feedbacks) {
        this.feedbacks = feedbacks;
    }

    public boolean isReviewTaskForProfessor() {
        return reviewTaskForProfessor;
    }
}
