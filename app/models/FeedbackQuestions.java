package models;


import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "feedback_questions")
public class FeedbackQuestions implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_question_id")
    private Long questionId;

    @ManyToOne
    @JoinColumn(name = "assignment_id", referencedColumnName = "assignment_id", nullable = false)
    private Assignments assignment;  // Assignment this question belongs to

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    public FeedbackQuestions() {}

    public FeedbackQuestions(Long questionId, Assignments assignment, String questionText) {
        this.questionId = questionId;
        this.assignment = assignment;
        this.questionText = questionText;
    }

    public Assignments getAssignment() {
        return assignment;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public String getQuestionText() {
        return questionText;
    }
}
