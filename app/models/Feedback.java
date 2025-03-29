package models;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "feedback")
public class Feedback implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "review_task_id", referencedColumnName = "review_task_id", nullable = false)
    private ReviewTask reviewTask;  // The review task this feedback belongs to

    @ManyToOne(optional = true)
    @JoinColumn(name = "feedback_question_id", referencedColumnName = "feedback_question_id", nullable = false)
    private FeedbackQuestion question;  // The feedback question being answered

    @Column(name = "score", nullable = false)
    private int score;  // Score (0-10)

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

    public FeedbackQuestion getQuestion() {
        return question;
    }

    public int getScore() {
        return score;
    }

    public String getFeedbackText() {
        return feedbackText;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setReviewTask(ReviewTask reviewTask) {
        this.reviewTask = reviewTask;
    }

    public void setQuestion(FeedbackQuestion question) {
        this.question = question;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setFeedbackText(String feedbackText) {
        this.feedbackText = feedbackText;
    }
}
