package services.export;


import models.ReviewTask;
import models.dto.AssignmentExportDTO;
import models.dto.FeedbackDTO;
import models.dto.GroupSubmissionDTO;
import models.dto.MemberSubmissionDTO;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.core.ReviewTaskService;
import services.core.ReviewTaskServiceImpl;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ExcelExportServiceImpl implements ExportService {

    private final ReviewTaskService reviewTaskService;
    private static final Logger log = LoggerFactory.getLogger(ExcelExportServiceImpl.class);

    @Inject
    public ExcelExportServiceImpl(ReviewTaskService reviewTaskService) {
        this.reviewTaskService = reviewTaskService;
    }

    @Override
    public CompletableFuture<List<AssignmentExportDTO>> getAssignmentExportData(String courseCode, Long assignmentId) {
        return reviewTaskService.getReviewTasks(assignmentId).thenApply(
                reviewTasks -> {
                    String assignmentTitle = reviewTasks.get(0).getAssignment().getTitle();
                    String courseName = reviewTasks.get(0).getAssignment().getCourse().getCourseName();

                    Map<Long, List<ReviewTask>> groupedTasks = ((ReviewTaskServiceImpl) reviewTaskService).groupReviewTasksByGroup(reviewTasks);
                    List<GroupSubmissionDTO> groupDTOs = ((ReviewTaskServiceImpl) reviewTaskService).generateSubmissionInfoInEachGroupDTOs(groupedTasks);

                    List<AssignmentExportDTO> exportDTOS = new ArrayList<>();

                    for (GroupSubmissionDTO groupDTO : groupDTOs) {
                        for (MemberSubmissionDTO memberDTO : groupDTO.getMembers()) {
                            AssignmentExportDTO exportDTO = new AssignmentExportDTO();
                            exportDTO.setCourseCode(courseCode);
                            exportDTO.setCourseName(courseName);
                            exportDTO.setAssignmentTitle(assignmentTitle);
                            exportDTO.setGroupId(groupDTO.getGroupId());
                            exportDTO.setGroupName(groupDTO.getGroupName());
                            exportDTO.setStudentId(memberDTO.getUserId());
                            exportDTO.setStudentName(memberDTO.getUserName());
                            exportDTO.setStatus(memberDTO.getStatus());
                            exportDTO.setAverageFeedbackScore(memberDTO.getAverageFeedbackScore());
                            exportDTOS.add(exportDTO);
                        }
                    }
                    return exportDTOS;
                }
        );
    }


    @Override
    public CompletableFuture<byte[]> exportToExcel(List<AssignmentExportDTO> assignmentExportDTOS) {
        return CompletableFuture.supplyAsync(() -> {
            try(XSSFWorkbook workbook = new XSSFWorkbook()){
                XSSFSheet sheet = workbook.createSheet("Review Report");

                int rowNum = 0;
                Row header = sheet.createRow(rowNum++);
                header.createCell(0).setCellValue("Course Code");
                header.createCell(1).setCellValue("Assignment Title");
                header.createCell(2).setCellValue("Group ID");
                header.createCell(3).setCellValue("Group Name");
                header.createCell(4).setCellValue("Student ID");
                header.createCell(5).setCellValue("Student Name");
                header.createCell(6).setCellValue("Status");
                header.createCell(7).setCellValue("Average Feedback Score");

                for (AssignmentExportDTO dto : assignmentExportDTOS) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(dto.getCourseCode());
                    row.createCell(1).setCellValue(dto.getAssignmentTitle());
                    row.createCell(2).setCellValue(dto.getGroupId());
                    row.createCell(3).setCellValue(dto.getGroupName());
                    row.createCell(4).setCellValue(dto.getStudentId());
                    row.createCell(5).setCellValue(dto.getStudentName());
                    row.createCell(6).setCellValue(dto.getStatus());
                    row.createCell(7).setCellValue(dto.getAverageFeedbackScore());
                }

                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    workbook.write(outputStream);
                    return outputStream.toByteArray();
                }
            }catch (Exception e){
                log.error("Error generating excel report", e);
                throw new RuntimeException("Error generating excel report", e);
            }
        });
    }

    @Override
    public CompletableFuture<byte[]> exportFeedbackForStudent(List<FeedbackDTO> feedbacks, String studentName, Long studentId, String email, String status, float averageFeedbackScore) {
        return CompletableFuture.supplyAsync(() -> {
            try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                XSSFSheet sheet = workbook.createSheet("Feedback Report");

                int rowNum = 0;

                // Add student metadata
                sheet.createRow(rowNum++).createCell(0).setCellValue("Student ID:");
                sheet.getRow(0).createCell(1).setCellValue(studentId);

                sheet.createRow(rowNum++).createCell(0).setCellValue("Student Name:");
                sheet.getRow(1).createCell(1).setCellValue(studentName);

                sheet.createRow(rowNum++).createCell(0).setCellValue("Email:");
                sheet.getRow(2).createCell(1).setCellValue(email);

                sheet.createRow(rowNum++).createCell(0).setCellValue("Status:");
                sheet.getRow(3).createCell(1).setCellValue(status);

                sheet.createRow(rowNum++).createCell(0).setCellValue("Average Feedback Score:");
                sheet.getRow(4).createCell(1).setCellValue(averageFeedbackScore);

                rowNum++; // Blank row

                // Header row for feedback entries
                Row header = sheet.createRow(rowNum++);
                header.createCell(0).setCellValue("Reviewer ID");
                header.createCell(1).setCellValue("Reviewer Name");
                header.createCell(2).setCellValue("Question");
                header.createCell(3).setCellValue("Score");
                header.createCell(4).setCellValue("Feedback");

                // Populate feedback rows
                for (FeedbackDTO feedback : feedbacks) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(feedback.getReviewerId());
                    row.createCell(1).setCellValue(feedback.getReviewerName());
                    row.createCell(2).setCellValue(feedback.getQuestionText());
                    row.createCell(3).setCellValue(feedback.getMaxScore());
                    row.createCell(4).setCellValue(feedback.getFeedbackText());
                }

                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    workbook.write(out);
                    return out.toByteArray();
                }
            } catch (Exception e) {
                log.error("Failed to generate feedback report for {}: {}", studentName, e.getMessage(), e);
                throw new RuntimeException("Error generating feedback report", e);
            }
        });
    }
}
