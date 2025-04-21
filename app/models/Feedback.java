package models;

import jakarta.persistence.*;
import java.io.Serializable;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Feedback is an entity class that represents feedback given by a student on a review task. It
 * contains fields for the feedback's ID, the review task it belongs to, the feedback question being
 * answered, the score given (0-100), and the written feedback text. The class also includes methods
 * for getting and setting these fields.
 */
@Entity
@Table(name = "feedback")
public class Feedback implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "review_task_id", referencedColumnName = "review_task_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ReviewTask reviewTask;  // The review task this feedback belongs to

    @ManyToOne
    @JoinColumn(name = "feedback_question_id", referencedColumnName = "feedback_question_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private FeedbackQuestion question;  // The feedback question being answered

    @Column(name = "score", nullable = false)
    private int score;  // Score (0-100)

    @Column(name = "feedback", nullable = false, columnDefinition = "TEXT")
    private String feedbackText;  // Written feedback

    public Feedback() {}

    public Feedback(ReviewTask reviewTask, FeedbackQuestion question, int score, String feedbackText) {
        this.reviewTask = reviewTask;
        this.question = question;
        this.score = score;
        this.feedbackText = feedbackText;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FeedbackQuestion getQuestion() {
        return question;
    }

    public void setQuestion(FeedbackQuestion question) {
        this.question = question;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getFeedbackText() {
        return feedbackText;
    }

    public void setFeedbackText(String feedbackText) {
        this.feedbackText = feedbackText;
    }

    public ReviewTask getReviewTask() {
        return reviewTask;
    }

    public void setReviewTask(ReviewTask reviewTask) {
        this.reviewTask = reviewTask;
    }
}
