package models.dto;

public class FeedbackDTO {
    private int score;
    private String feedbackText;
    private String questionText;
    private Long reviewerId;
    private String reviewerName;

    public FeedbackDTO() {}

    public FeedbackDTO(int score, String feedbackText, String questionText, Long reviewerId, String reviewerName) {
        this.score = score;
        this.feedbackText = feedbackText;
        this.questionText = questionText;
        this.reviewerId = reviewerId;
        this.reviewerName = reviewerName;
    }

    public int getScore() {
        return score;
    }

    public String getFeedbackText() {
        return feedbackText;
    }

    public String getQuestionText() {
        return questionText;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public String getReviewerName() {
        return reviewerName;
    }
}
