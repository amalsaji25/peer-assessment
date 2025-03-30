package models.dto;

import java.util.List;

public class FeedbacksReceivedDTO {
    private String assignmentTitle;
    private String peerLabel;
    private int totalMarks;
    private int obtainedMarks;
    private List<FeedbackDTO> feedbacks;

    public FeedbacksReceivedDTO() {}

    public FeedbacksReceivedDTO(String assignmentTitle, String peerLabel, int totalMarks, int obtainedMarks, List<FeedbackDTO> feedbacks) {
        this.assignmentTitle = assignmentTitle;
        this.peerLabel = peerLabel;
        this.totalMarks = totalMarks;
        this.obtainedMarks = obtainedMarks;
        this.feedbacks = feedbacks;
    }

    public String getAssignmentTitle() {
        return assignmentTitle;
    }

    public void setAssignmentTitle(String assignmentTitle) {
        this.assignmentTitle = assignmentTitle;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(int totalMarks) {
        this.totalMarks = totalMarks;
    }

    public int getObtainedMarks() {
        return obtainedMarks;
    }

    public void setObtainedMarks(int obtainedMarks) {
        this.obtainedMarks = obtainedMarks;
    }

    public List<FeedbackDTO> getFeedbacks() {
        return feedbacks;
    }

    public void setFeedbacks(List<FeedbackDTO> feedbacks) {
        this.feedbacks = feedbacks;
    }

    public String getPeerLabel() {
        return peerLabel;
    }

    public void setPeerLabel(String peerLabel) {
        this.peerLabel = peerLabel;
    }
}
