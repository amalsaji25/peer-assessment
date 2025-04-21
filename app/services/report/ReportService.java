package services.report;

import java.util.concurrent.CompletableFuture;
import models.dto.ReportDTO;

/**
 * ReportService is an interface that defines the contract for generating reports. It provides a
 * method to generate a report based on the assignment ID and user ID.
 */
public interface ReportService {

  CompletableFuture<ReportDTO> generateReport(Long AssignmentId, Long userId);
}
