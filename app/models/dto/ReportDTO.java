package models.dto;

import java.util.List;
import java.util.Map;

/**
 * ReportDTO is a data transfer object (DTO) that represents the report data for an assignment.
 * It contains fields for assignment details, course information, evaluation statistics, and
 * feedbacks for students.
 */
public class ReportDTO {
    public float getStudentAverageScore;
    Map<String, Float> classAverages;
    private Long assignmentId;
    private String assignmentTitle;
    private String courseName;
    private String courseCode;
    private String courseSection;
    private String term;
    private int totalTeams;
    private int totalEvaluations;
    private int completedEvaluations;
    private int incompleteEvaluations;
    private Long userId;
    private String userName;
    private String groupName;
    private float overallClassAverage;
    private Map<Long, List<FeedbackDTO>> feedbacksForStudent;
    private List<GroupSubmissionDTO> groups;

    public String getAssignmentTitle() {
        return assignmentTitle;
    }

    public void setAssignmentTitle(String assignmentTitle) {
        this.assignmentTitle = assignmentTitle;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseSection() {
        return courseSection;
    }

    public void setCourseSection(String courseSection) {
        this.courseSection = courseSection;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public int getTotalTeams() {
        return totalTeams;
    }

    public void setTotalTeams(int totalTeams) {
        this.totalTeams = totalTeams;
    }

    public int getTotalEvaluations() {
        return totalEvaluations;
    }

    public void setTotalEvaluations(int totalEvaluations) {
        this.totalEvaluations = totalEvaluations;
    }

    public int getCompletedEvaluations() {
        return completedEvaluations;
    }

    public void setCompletedEvaluations(int completedEvaluations) {
        this.completedEvaluations = completedEvaluations;
    }

    public int getIncompleteEvaluations() {
        return incompleteEvaluations;
    }

    public void setIncompleteEvaluations(int incompleteEvaluations) {
        this.incompleteEvaluations = incompleteEvaluations;
    }

    public List<GroupSubmissionDTO> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupSubmissionDTO> groups) {
        this.groups = groups;
    }
    public Long getAssignmentId() {
        return assignmentId;
    }
    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public void setStudentId(Long userId) {
        this.userId = userId;
    }

    public void setStudentName(String userName) {
        this.userName = userName;
    }

    public void setStudentGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setClassAverage(float overallClassAverage) {
        this.overallClassAverage = overallClassAverage;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getGroupName() {
        return groupName;
    }

    public float getOverallClassAverage() {
        return overallClassAverage;
    }

    public Map<Long, List<FeedbackDTO>> getGroupedAnonymizedFeedbacks() {
        return feedbacksForStudent;
    }

    public void setGroupedAnonymizedFeedbacks(Map<Long, List<FeedbackDTO>> feedbacksForStudent) {
        this.feedbacksForStudent = feedbacksForStudent;
    }

    public Map<String, Float> getClassAveragePerQuestion() {
        return classAverages;
    }

    public void setClassAveragePerQuestion(Map<String, Float> classAverages) {
        this.classAverages = classAverages;
    }

    public float getGetStudentAverageScore() {
        return getStudentAverageScore;
    }

    public void setStudentAverageScore(float getStudentAverageScore) {
        this.getStudentAverageScore = getStudentAverageScore;
    }
}
