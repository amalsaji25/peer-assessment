package services.export;

import models.Assignment;
import models.Course;
import models.ReviewTask;
import models.User;
import models.dto.AssignmentExportDTO;
import models.dto.GroupSubmissionDTO;
import models.dto.MemberSubmissionDTO;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import services.core.ReviewTaskService;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ExcelExportServiceImplTest {

    @Mock private ReviewTaskService mockReviewTaskService;
    private ExcelExportServiceImpl excelExportService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        excelExportService = new ExcelExportServiceImpl(mockReviewTaskService);
    }

    @Test
    public void testGetAssignmentExportData_returnsCorrectList() throws Exception {
        Assignment assignment = new Assignment();
        assignment.setTitle("A1");

        Course course = new Course();
        course.setCourseCode("CS101");
        course.setCourseName("Intro to CS");
        assignment.setCourse(course);

        ReviewTask reviewTask = new ReviewTask(
                assignment,
                new User(),
                new User(),
                null,
                1L,
                "Group 1",
                2,
                false
        );
        List<ReviewTask> mockTasks = List.of(reviewTask);

        // Use default constructor and set values manually (per DTO definition)
        MemberSubmissionDTO member = new MemberSubmissionDTO();
        member.setUserId(101L);
        member.setUserName("John Doe");
        member.setStatus("COMPLETED");
        member.setAverageFeedbackScore(8.5f);
        member.setEvaluationMatrix(new ArrayList<>());

        GroupSubmissionDTO groupDTO = new GroupSubmissionDTO();
        groupDTO.setGroupId(1L);
        groupDTO.setGroupName("Group 1");
        groupDTO.setMembers(List.of(member));

        when(mockReviewTaskService.getReviewTasks(1L)).thenReturn(CompletableFuture.completedFuture(mockTasks));
        when(mockReviewTaskService.groupReviewTasksByGroup(mockTasks)).thenReturn(Map.of(1L, mockTasks));
        when(mockReviewTaskService.generateSubmissionInfoInEachGroupDTOs(anyMap())).thenReturn(List.of(groupDTO));

        List<AssignmentExportDTO> exportList = excelExportService.getAssignmentExportData(1L).get();
        assertEquals(1, exportList.size());
        assertEquals("John Doe", exportList.get(0).getStudentName());
        assertEquals("CS101", exportList.get(0).getCourseCode());
    }

    @Test
    public void testExportToExcel_returnsNonEmptyByteArray() throws Exception {
        AssignmentExportDTO dto = new AssignmentExportDTO();
        dto.setStudentId(101L);
        dto.setStudentName("Jane Smith");
        dto.setGroupId(1L);
        dto.setGroupName("Group 1");
        dto.setCourseCode("CS101");
        dto.setCourseName("Intro to CS");
        dto.setAssignmentTitle("Assignment 1");
        dto.setAverageFeedbackScore(9.0f);

        // Create EvaluationMatrixDTO via constructor only (DTO supports it)
        MemberSubmissionDTO.EvaluationMatrixDTO matrix =
                new MemberSubmissionDTO.EvaluationMatrixDTO("Q1", List.of(9), 9.0f);
        dto.setEvaluationMatrix(List.of(matrix));

        byte[] result = excelExportService.exportToExcel(List.of(dto)).get();
        assertNotNull(result);
        assertTrue(result.length > 0);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            assertEquals("Summary Report", workbook.getSheetAt(0).getSheetName());
        }
    }
}