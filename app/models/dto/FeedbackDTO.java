package models.dto;

public class FeedbackDTO {
    private Long feedbackId;
    private int maxScore;
    private int obtainedScore;
    private String feedbackText;
    private String questionText;
    private Long reviewerId;
    private String reviewerName;

    public FeedbackDTO() {}

    public FeedbackDTO(int maxScore, String feedbackText, String questionText, Long reviewerId, String reviewerName) {
        this.maxScore = maxScore;
        this.feedbackText = feedbackText;
        this.questionText = questionText;
        this.reviewerId = reviewerId;
        this.reviewerName = reviewerName;
    }

    public FeedbackDTO(int maxScore, String feedbackText, String questionText) {
        this.maxScore = maxScore;
        this.feedbackText = feedbackText;
        this.questionText = questionText;
    }

    public FeedbackDTO(Long feedbackId, int obtainedScore, int maxScore, String questionText, String feedbackText) {
        this.feedbackId = feedbackId;
        this.obtainedScore = obtainedScore;
        this.maxScore = maxScore;
        this.questionText = questionText;
        this.feedbackText = feedbackText;
    }

    public FeedbackDTO(Long feedbackId, String feedbackText, int score) {
        this.feedbackId = feedbackId;
        this.feedbackText = feedbackText;
        this.obtainedScore = score;
    }

    public int getMaxScore() {
        return maxScore;
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

    public Long getFeedbackId() {
        return feedbackId;
    }

    public int getObtainedScore() {
        return obtainedScore;
    }
}
