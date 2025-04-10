package services.export;

import models.dto.AssignmentExportDTO;
import models.dto.FeedbackDTO;
import models.dto.GroupSubmissionDTO;
import models.dto.MemberSubmissionDTO;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * ExportService is an interface that defines the contract for export services. It provides methods
 * to retrieve assignment export data, export data to Excel, and export feedback for a student.
 */
public interface ExportService {
    CompletableFuture<List<AssignmentExportDTO>> getAssignmentExportData(Long assignmentId);

    CompletableFuture<byte[]> exportToExcel(List<AssignmentExportDTO> assignmentExportDTOS);

    CompletableFuture<byte[]> exportFeedbackForStudent(Map<Long,List<FeedbackDTO>> feedbacks, String studentName, Long studentId, String email, String status, float averageFeedbackScore, float maxAverageFeedbackScore, List<MemberSubmissionDTO.EvaluationMatrixDTO> evaluationMatrix, Map<String, Float> classAverages, float overallClassAverage, List<Float> reviewerAverages);
}
