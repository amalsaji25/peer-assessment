package models.dto;

import java.util.List;
import java.util.Map;

/**
 * MemberSubmissionDTO is a data transfer object (DTO) that represents the submission details of a
 * member in an assignment. It contains fields for the member's user ID, username, email, average
 * feedback score, maximum average feedback score for the review task, status, feedbacks by reviewer,
 * evaluation matrix, reviewer names, reviewer averages, feedbacks per question, private comments,
 * reviewers response count, class averages for each question, and overall class average.
 */
public class MemberSubmissionDTO {
    public Long userId;
    public String userName;
    public String email;
    public float averageFeedbackScore;
    public float maximumAverageFeedbackScoreForReviewTask;
    public String status;
    private Map<Long, List<FeedbackDTO>> feedbacksByReviewer;
    private List<EvaluationMatrixDTO> evaluationMatrix;
    private List<String> reviewerNames;
    private List<Float> reviewerAverages;
    private List<FeedbackDTO> feedbacksPerQuestion;
    private List<FeedbackDTO> privateComments;
    private int reviewersResponseCount;
    private Map<String, Float> classAverages;
    private float overallClassAverage;

    public Map<Long, List<FeedbackDTO>> getFeedbacks() {
        return feedbacksByReviewer;
    }

    public void setFeedbacks(Map<Long, List<FeedbackDTO>> feedbacks) {
        this.feedbacksByReviewer = feedbacks;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public float getAverageFeedbackScore() {
        return averageFeedbackScore;
    }

    public void setAverageFeedbackScore(float averageFeedbackScore) {
        this.averageFeedbackScore = averageFeedbackScore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<Long, List<FeedbackDTO>> getFeedbacksByReviewer() {
        return feedbacksByReviewer;
    }

    public void setFeedbacksByReviewer(Map<Long, List<FeedbackDTO>> feedbacksByReviewer) {
        this.feedbacksByReviewer = feedbacksByReviewer;
    }

    public List<EvaluationMatrixDTO> getEvaluationMatrix() {
        return evaluationMatrix;
    }

    public void setEvaluationMatrix(List<EvaluationMatrixDTO> evaluationMatrix) {
        this.evaluationMatrix = evaluationMatrix;
    }

    public List<String> getReviewerNames() {
        return reviewerNames;
    }

    public void setReviewerNames(List<String> reviewerNames) {
        this.reviewerNames = reviewerNames;
    }

    public List<Float> getReviewerAverages() {
        return reviewerAverages;
    }

    public void setReviewerAverages(List<Float> reviewerAverages) {
        this.reviewerAverages = reviewerAverages;
    }

    public List<FeedbackDTO> getFeedbacksPerQuestion() {
        return feedbacksPerQuestion;
    }

    public void setFeedbacksPerQuestion(List<FeedbackDTO> feedbacksPerQuestion) {
        this.feedbacksPerQuestion = feedbacksPerQuestion;
    }

    public List<FeedbackDTO> getPrivateComments() {
        return privateComments;
    }

    public void setPrivateComments(List<FeedbackDTO> privateComments) {
        this.privateComments = privateComments;
    }

    public int getReviewersResponseCount() {
        return reviewersResponseCount;
    }

    public void setReviewersResponseCount(int reviewersResponseCount) {
        this.reviewersResponseCount = reviewersResponseCount;
    }

    public float getMaximumAverageFeedbackScoreForReviewTask() {
        return maximumAverageFeedbackScoreForReviewTask;
    }

    public void setMaximumAverageFeedbackScoreForReviewTask(float maximumAverageFeedbackScoreForReviewTask) {
        this.maximumAverageFeedbackScoreForReviewTask = maximumAverageFeedbackScoreForReviewTask;
    }

    public void setClassAveragesForEachQuestion(Map<String, Float> classAverages) {
        this.classAverages = classAverages;
    }

    public Map<String, Float> getClassAverages() {
        return classAverages;
    }

    public float getOverallClassAverage() {
        return overallClassAverage;
    }

    public void setOverallClassAverage(float overallClassAverage) {
        this.overallClassAverage = overallClassAverage;
    }

    public static class EvaluationMatrixDTO {
        private String feedbackQuestion;
        private List<Integer> marksPerReviewer;
        private float averageMarkForQuestion;

        public EvaluationMatrixDTO(){}

        public EvaluationMatrixDTO(String question, List<Integer> marksPerReviewer, float avg) {
            this.feedbackQuestion = question;
            this.marksPerReviewer = marksPerReviewer;
            this.averageMarkForQuestion = avg;
        }

        public String getFeedbackQuestion() {
            return feedbackQuestion;
        }

        public void setFeedbackQuestion(String feedbackQuestion) {
            this.feedbackQuestion = feedbackQuestion;
        }

        public List<Integer> getMarksPerReviewer() {
            return marksPerReviewer;
        }

        public void setMarksPerReviewer(List<Integer> marksPerReviewer) {
            this.marksPerReviewer = marksPerReviewer;
        }

        public float getAverageMarkForQuestion() {
            return averageMarkForQuestion;
        }

        public void setAverageMarkForQuestion(float averageMarkForQuestion) {
            this.averageMarkForQuestion = averageMarkForQuestion;
        }
    }
}
