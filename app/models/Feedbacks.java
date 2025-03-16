package models;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "feedback")
public class Feedbacks implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "review_task_id", referencedColumnName = "review_task_id", nullable = false)
    private ReviewTasks reviewTask;  // The review task this feedback belongs to

    @ManyToOne
    @JoinColumn(name = "feedback_question_id", referencedColumnName = "feedback_question_id", nullable = false)
    private FeedbackQuestions question;  // The feedback question being answered

    @Column(name = "score", nullable = false)
    private int score;  // Score (0-10)

    @Column(name = "feedback", nullable = false, columnDefinition = "TEXT")
    private String feedbackText;  // Written feedback

    public Feedbacks() {}

    public Feedbacks(ReviewTasks reviewTask, FeedbackQuestions question, int score, String feedbackText) {
        this.reviewTask = reviewTask;
        this.question = question;
        this.score = score;
        this.feedbackText = feedbackText;
    }

    public Long getId() {
        return id;
    }

    public ReviewTasks getReviewTask() {
        return reviewTask;
    }

    public FeedbackQuestions getQuestion() {
        return question;
    }

    public int getScore() {
        return score;
    }

    public String getFeedbackText() {
        return feedbackText;
    }
}
