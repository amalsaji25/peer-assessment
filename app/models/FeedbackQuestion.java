package models;


import jakarta.persistence.*;
import java.io.Serializable;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * FeedbackQuestion is an entity class that represents a feedback question in the system. It
 * contains fields for the question's ID, the assignment it belongs to, the question text, and the
 * maximum marks for the question. The class also includes methods for getting and setting these
 * fields.
 */
@Entity
@Table(name = "feedback_questions")
public class FeedbackQuestion implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_question_id")
    private Long questionId;

    @ManyToOne
    @JoinColumn(name = "assignment_id", referencedColumnName = "assignment_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Assignment assignment;  // Assignment this question belongs to

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "max_marks", nullable = false)
    private int maxMarks;

    public FeedbackQuestion() {}

    public FeedbackQuestion(Long questionId, Assignment assignment, String questionText, int maxMarks) {
        this.questionId = questionId;
        this.assignment = assignment;
        this.questionText = questionText;
        this.maxMarks = maxMarks;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public int getMaxMarks() {return maxMarks;}

    public void setMaxMarks(int maxMarks) {
        this.maxMarks = maxMarks;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }
}
