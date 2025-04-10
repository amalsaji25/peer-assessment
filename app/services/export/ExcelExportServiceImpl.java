package services.export;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.inject.Inject;
import models.ReviewTask;
import models.dto.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.core.ReviewTaskService;

/**
 * ExcelExportServiceImpl is a service class that implements the ExportService interface. It
 * provides methods to generate Excel reports for assignment exports and feedback exports.
 */
public class ExcelExportServiceImpl implements ExportService {

  private static final Logger log = LoggerFactory.getLogger(ExcelExportServiceImpl.class);
  private final ReviewTaskService reviewTaskService;

  @Inject
  public ExcelExportServiceImpl(ReviewTaskService reviewTaskService) {
    this.reviewTaskService = reviewTaskService;
  }

  /**
   * Retrieves assignment export data for a given assignment ID.
   *
   * @param assignmentId the ID of the assignment
   * @return a CompletableFuture containing a list of AssignmentExportDTO
   */
  @Override
  public CompletableFuture<List<AssignmentExportDTO>> getAssignmentExportData(Long assignmentId) {
    return reviewTaskService
        .getReviewTasks(assignmentId)
        .thenApply(
            reviewTasks -> {
              String assignmentTitle = reviewTasks.get(0).getAssignment().getTitle();
              String courseName = reviewTasks.get(0).getAssignment().getCourse().getCourseName();
              String courseCode = reviewTasks.get(0).getAssignment().getCourse().getCourseCode();

              Map<Long, List<ReviewTask>> groupedTasks =
                  reviewTaskService.groupReviewTasksByGroup(reviewTasks);
              List<GroupSubmissionDTO> groupDTOs =
                  reviewTaskService.generateSubmissionInfoInEachGroupDTOs(groupedTasks);

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
                  exportDTO.setEvaluationMatrix(memberDTO.getEvaluationMatrix());
                  exportDTOS.add(exportDTO);
                }
              }
              return exportDTOS;
            });
  }

  /**
   * Exports assignment data to an Excel file.
   *
   * @param assignmentExportDTOS the list of AssignmentExportDTO
   * @return a CompletableFuture containing the byte array of the Excel file
   */
  @Override
  public CompletableFuture<byte[]> exportToExcel(List<AssignmentExportDTO> assignmentExportDTOS) {
    return CompletableFuture.supplyAsync(
        () -> {
          try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Summary Report");
            sheet.setZoom(120); // Set zoom

            int rowNum = 0;

            // Fonts
            XSSFFont boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldFont.setFontHeightInPoints((short) 12);

            XSSFFont normalFont = workbook.createFont();
            normalFont.setFontHeightInPoints((short) 12);

            // Styles
            XSSFCellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(boldFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THICK);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            XSSFCellStyle normalStyle = workbook.createCellStyle();
            normalStyle.setFont(normalFont);
            normalStyle.setVerticalAlignment(VerticalAlignment.TOP);
            normalStyle.setBorderTop(BorderStyle.THIN);
            normalStyle.setBorderBottom(BorderStyle.THIN);
            normalStyle.setBorderLeft(BorderStyle.THIN);
            normalStyle.setBorderRight(BorderStyle.THIN);

            // Header row
            Row header = sheet.createRow(rowNum++);
            int cellIdx = 0;

            String[] staticHeaders = {"ID", "Name", "Group"};
            for (String title : staticHeaders) {
              Cell cell = header.createCell(cellIdx++);
              cell.setCellValue(title);
              cell.setCellStyle(headerStyle);
            }

            // Dynamic feedback questions
            List<String> questions =
                assignmentExportDTOS.get(0).getEvaluationMatrix().stream()
                    .map(MemberSubmissionDTO.EvaluationMatrixDTO::getFeedbackQuestion)
                    .toList();

            for (String q : questions) {
              Cell cell = header.createCell(cellIdx++);
              cell.setCellValue(q);
              cell.setCellStyle(headerStyle);
            }

            Cell avgCell = header.createCell(cellIdx);
            avgCell.setCellValue("Average");
            avgCell.setCellStyle(headerStyle);

            // Data rows
            for (AssignmentExportDTO dto : assignmentExportDTOS) {
              Row row = sheet.createRow(rowNum++);
              int dataIdx = 0;

              row.createCell(dataIdx).setCellValue(dto.getStudentId());
              row.getCell(dataIdx++).setCellStyle(normalStyle);

              row.createCell(dataIdx).setCellValue(dto.getStudentName());
              row.getCell(dataIdx++).setCellStyle(normalStyle);

              row.createCell(dataIdx).setCellValue("Group " + dto.getGroupId());
              row.getCell(dataIdx++).setCellStyle(normalStyle);

              Map<String, Float> scoreMap =
                  dto.getEvaluationMatrix().stream()
                      .collect(
                          Collectors.toMap(
                              MemberSubmissionDTO.EvaluationMatrixDTO::getFeedbackQuestion,
                              MemberSubmissionDTO.EvaluationMatrixDTO::getAverageMarkForQuestion));

              for (String q : questions) {
                Cell scoreCell = row.createCell(dataIdx++);
                scoreCell.setCellValue(scoreMap.getOrDefault(q, 0f));
                scoreCell.setCellStyle(normalStyle);
              }

              Cell avg = row.createCell(dataIdx);
              avg.setCellValue(dto.getAverageFeedbackScore());
              avg.setCellStyle(normalStyle);
            }

            // Auto-size all columns
            int totalCols = 3 + questions.size() + 1;
            for (int i = 0; i < totalCols; i++) {
              sheet.autoSizeColumn(i);
            }

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
              workbook.write(outputStream);
              return outputStream.toByteArray();
            }
          } catch (Exception e) {
            throw new RuntimeException("Error generating summary excel report", e);
          }
        });
  }

  /**
   * Exports feedback data for a specific student to an Excel file.
   *
   * @param feedbacks the map of feedbacks grouped by reviewer ID
   * @param studentName the name of the student
   * @param studentId the ID of the student
   * @param email the email of the student
   * @param status the status of the student
   * @param averageFeedbackScore the average feedback score of the student
   * @param maxAverageFeedbackScore the maximum average feedback score
   * @param evalautionMatrix the evaluation matrix for the assignment
   * @param classAverages the class averages for each question
   * @param overallClassAverage the overall class average score
   * @param reviewerAverages the list of reviewer averages
   * @return a CompletableFuture containing the byte array of the Excel file
   */
  @Override
  public CompletableFuture<byte[]> exportFeedbackForStudent(
      Map<Long, List<FeedbackDTO>> feedbacks,
      String studentName,
      Long studentId,
      String email,
      String status,
      float averageFeedbackScore,
      float maxAverageFeedbackScore,
      List<MemberSubmissionDTO.EvaluationMatrixDTO> evalautionMatrix,
      Map<String, Float> classAverages,
      float overallClassAverage,
      List<Float> reviewerAverages) {
    return CompletableFuture.supplyAsync(
        () -> {
          try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            XSSFSheet sheet = workbook.createSheet("Feedback Report");
            sheet.setZoom(120); // Set zoom to 120%

            // Define bold font (12pt)
            XSSFFont boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldFont.setFontHeightInPoints((short) 12);

            // Define normal font (12pt)
            XSSFFont normalFont = workbook.createFont();
            normalFont.setFontHeightInPoints((short) 12);

            // Define borders
            XSSFCellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(boldFont);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THICK); // Thick bottom border
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            XSSFCellStyle labelStyle = workbook.createCellStyle();
            labelStyle.setFont(boldFont);
            labelStyle.setAlignment(HorizontalAlignment.LEFT);

            XSSFCellStyle normalStyle = workbook.createCellStyle();
            normalStyle.setFont(normalFont);
            normalStyle.setBorderTop(BorderStyle.THIN);
            normalStyle.setBorderBottom(BorderStyle.THIN);
            normalStyle.setBorderLeft(BorderStyle.THIN);
            normalStyle.setBorderRight(BorderStyle.THIN);
            normalStyle.setVerticalAlignment(VerticalAlignment.TOP);

            int rowNum = 0;

            // Student Metadata Rows
            String[][] metadata = {
              {"Student ID:", String.valueOf(studentId)},
              {"Student Name:", studentName},
              {"Email:", email},
              {"Status:", status},
              {
                "Feedback Score (Obtained / Total):",
                String.format("%.1f / %.1f", averageFeedbackScore, maxAverageFeedbackScore)
              },
              {
                "Overall Class Average:",
                String.format("%.1f / %.1f", overallClassAverage, maxAverageFeedbackScore)
              }
            };

            for (String[] entry : metadata) {
              Row row = sheet.createRow(rowNum++);
              Cell labelCell = row.createCell(0);
              labelCell.setCellValue(entry[0]);
              labelCell.setCellStyle(labelStyle);

              Cell valueCell = row.createCell(1);
              valueCell.setCellValue(entry[1]);
              valueCell.setCellStyle(normalStyle);
            }

            rowNum++; // Add blank row

            // Header Row
            Row header = sheet.createRow(rowNum++);
            String[] headers = {"Reviewer ID", "Reviewer Name", "Question", "Score", "Feedback"};
            for (int i = 0; i < headers.length; i++) {
              Cell cell = header.createCell(i);
              cell.setCellValue(headers[i]);
              cell.setCellStyle(headerStyle);
            }

            // Feedback Rows (Grouped by Reviewer)
            for (Map.Entry<Long, List<FeedbackDTO>> entry : feedbacks.entrySet()) {
              Long reviewerId = entry.getKey();
              List<FeedbackDTO> reviewerFeedbacks = entry.getValue();

              boolean first = true;
              for (FeedbackDTO feedback : reviewerFeedbacks) {
                if (feedback.getQuestionText().equalsIgnoreCase("Overall Feedback Comment"))
                  continue;
                Row row = sheet.createRow(rowNum++);
                for (int col = 0; col <= 4; col++) {
                  row.createCell(col).setCellStyle(normalStyle);
                }

                if (first) {
                  row.getCell(0).setCellValue(reviewerId);
                  row.getCell(1).setCellValue(feedback.getReviewerName());
                  first = false;
                }

                row.getCell(2).setCellValue(feedback.getQuestionText());
                row.getCell(3)
                    .setCellValue(
                        String.format(
                            "%d / %d", feedback.getObtainedScore(), feedback.getMaxScore()));
                row.getCell(4).setCellValue(feedback.getFeedbackText());
              }
              rowNum++; // Blank row between reviewers
            }

            rowNum++;

            // Question wise Summary Row
            Row questionSummaryHeader = sheet.createRow(rowNum++);
            Cell questionSummaryHeaderCell = questionSummaryHeader.createCell(0);
            questionSummaryHeaderCell.setCellValue("Question");
            questionSummaryHeaderCell.setCellStyle(headerStyle);

            Cell questionAverageScoreCell = questionSummaryHeader.createCell(1);
            questionAverageScoreCell.setCellValue("Average Feedback Score");
            questionAverageScoreCell.setCellStyle(headerStyle);

            Cell classAverageScoreCell = questionSummaryHeader.createCell(2);
            classAverageScoreCell.setCellValue("Class Average");
            classAverageScoreCell.setCellStyle(headerStyle);

            for (MemberSubmissionDTO.EvaluationMatrixDTO question : evalautionMatrix) {
              Row questionRow = sheet.createRow(rowNum++);
              Cell questionCell = questionRow.createCell(0);
              questionCell.setCellValue(question.getFeedbackQuestion());
              questionCell.setCellStyle(normalStyle);

              Cell averageScoreCell = questionRow.createCell(1);
              averageScoreCell.setCellValue(question.getAverageMarkForQuestion());
              averageScoreCell.setCellStyle(normalStyle);

              Cell classAverageCell = questionRow.createCell(2);
              classAverageCell.setCellValue(
                  classAverages.getOrDefault(question.getFeedbackQuestion(), 0f));
              classAverageCell.setCellStyle(normalStyle);
            }

            rowNum++;

            // Reviewer wise Summary Row

            Row reviewerSummaryHeader = sheet.createRow(rowNum++);
            Cell reviewerSummaryHeaderCell = reviewerSummaryHeader.createCell(0);
            reviewerSummaryHeaderCell.setCellValue("Reviewer Id");
            reviewerSummaryHeaderCell.setCellStyle(headerStyle);

            Cell reviewerNameHeaderCell = reviewerSummaryHeader.createCell(1);
            reviewerNameHeaderCell.setCellValue("Reviewer Name");
            reviewerNameHeaderCell.setCellStyle(headerStyle);

            Cell reviewerAverageScoreHeaderCell = reviewerSummaryHeader.createCell(2);
            reviewerAverageScoreHeaderCell.setCellValue("Provided Feedback Score");
            reviewerAverageScoreHeaderCell.setCellStyle(headerStyle);

            int reviewerIndex = 0;
            for (Map.Entry<Long, List<FeedbackDTO>> entry : feedbacks.entrySet()) {
              Long reviewerId = entry.getKey();
              List<FeedbackDTO> reviewerFeedbacks = entry.getValue();

              if (reviewerFeedbacks.isEmpty()) continue;

              Row row = sheet.createRow(rowNum++);

              Cell reviewerIdCell = row.createCell(0);
              reviewerIdCell.setCellValue(reviewerId);
              reviewerIdCell.setCellStyle(normalStyle);

              Cell reviewerNameCell = row.createCell(1);
              reviewerNameCell.setCellValue(reviewerFeedbacks.get(0).getReviewerName());
              reviewerNameCell.setCellStyle(normalStyle);

              Cell reviewerAverageScoreCell = row.createCell(2);
              reviewerAverageScoreCell.setCellValue(reviewerAverages.get(reviewerIndex));
              reviewerAverageScoreCell.setCellStyle(normalStyle);

              reviewerIndex++;
            }

            // Auto-size all columns
            for (int i = 0; i <= 4; i++) {
              sheet.autoSizeColumn(i);
            }

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
              workbook.write(out);
              return out.toByteArray();
            }
          } catch (Exception e) {
            log.error(
                "Failed to generate feedback report for {}: {}", studentName, e.getMessage(), e);
            throw new RuntimeException("Error generating feedback report", e);
          }
        });
  }
}
