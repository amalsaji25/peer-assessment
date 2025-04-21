package models.dto;

import java.util.List;

/**
 * AssignmentExportDTO is a data transfer object (DTO) that represents the data for exporting
 * assignment information, including course details, group and student information, and feedback
 * scores.
 */
public class AssignmentExportDTO {
    private String courseCode;
    private String courseName;
    private String assignmentTitle;
    private Long groupId;
    private String groupName;
    private Long studentId;
    private String studentName;
    private String status;
    private float averageFeedbackScore;
    private List<MemberSubmissionDTO.EvaluationMatrixDTO> evaluationMatrix;

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getAssignmentTitle() {
        return assignmentTitle;
    }

    public void setAssignmentTitle(String assignmentTitle) {
        this.assignmentTitle = assignmentTitle;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
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

    public List<MemberSubmissionDTO.EvaluationMatrixDTO> getEvaluationMatrix() {
        return evaluationMatrix;
    }

    public void setEvaluationMatrix(List<MemberSubmissionDTO.EvaluationMatrixDTO> evaluationMatrix) {
        this.evaluationMatrix = evaluationMatrix;
    }
}
