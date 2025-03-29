package services.export;

import models.dto.AssignmentExportDTO;
import models.dto.FeedbackDTO;
import models.dto.GroupSubmissionDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ExportService {
    CompletableFuture<List<AssignmentExportDTO>> getAssignmentExportData(String courseCode, Long assignmentId);

    CompletableFuture<byte[]> exportToExcel(List<AssignmentExportDTO> assignmentExportDTOS);

    CompletableFuture<byte[]> exportFeedbackForStudent(List<FeedbackDTO> feedbacks, String studentName, Long studentId, String email, String status, float averageFeedbackScore);
}
