package services.report;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import models.Assignment;
import models.Course;
import models.ReviewTask;
import models.User;
import models.dto.GroupSubmissionDTO;
import models.dto.MemberSubmissionDTO;
import models.dto.ReportDTO;
import models.enums.Status;
import org.junit.Before;
import org.junit.Test;
import services.core.ReviewTaskService;

public class ProfessorReportServiceImplTest {

    private ReviewTaskService reviewTaskService;
    private ProfessorReportServiceImpl professorReportService;

    @Before
    public void setUp() {
        reviewTaskService = mock(ReviewTaskService.class);
        professorReportService = new ProfessorReportServiceImpl(reviewTaskService);
    }

    @Test
    public void testGenerateReport_ReturnsCorrectDTO() {
        // Prepare mocks
        Course course = new Course();
        course.setCourseCode("CS101");
        course.setCourseSection("A");
        course.setCourseName("Intro to CS");
        course.setTerm("Fall 2025");

        Assignment assignment = new Assignment();
        assignment.setAssignmentId(1L);
        assignment.setTitle("Assignment 1");
        assignment.setCourse(course);

        User reviewer = new User(1L, "reviewer@example.com", "", "Alice", "R", "student");
        User reviewee = new User(2L, "reviewee@example.com", "", "Bob", "R", "student");

        ReviewTask task = new ReviewTask(assignment, reviewer, reviewee, Status.COMPLETED, 101L, "Team A", 2, false);
        List<ReviewTask> reviewTasks = List.of(task);

        when(reviewTaskService.getReviewTasks(1L)).thenReturn(CompletableFuture.completedFuture(reviewTasks));

        Map<Long, List<ReviewTask>> grouped = new HashMap<>();
        grouped.put(101L, reviewTasks);
        when(reviewTaskService.groupReviewTasksByGroup(reviewTasks)).thenReturn(grouped);

        GroupSubmissionDTO groupDTO = new GroupSubmissionDTO();
        groupDTO.setGroupId(101L);
        groupDTO.setGroupName("Team A");
        groupDTO.setMembers(List.of(new MemberSubmissionDTO())); // 1 member
        groupDTO.setReviewsCompleted(1); // 1 completed

        when(reviewTaskService.generateSubmissionInfoInEachGroupDTOs(grouped))
                .thenReturn(List.of(groupDTO));

        doAnswer(invocation -> {
            // simulate calculateClassAverages modifying in-place
            return null;
        }).when(reviewTaskService).calculateClassAverages(anyList());

        // Act
        ReportDTO report = professorReportService.generateReport(1L, 999L).join();

        // Assert
        assertEquals("Assignment 1", report.getAssignmentTitle());
        assertEquals("CS101", report.getCourseCode());
        assertEquals("Intro to CS", report.getCourseName());
        assertEquals("A", report.getCourseSection());
        assertEquals("Fall 2025", report.getTerm());
        assertEquals(1, report.getTotalTeams());
        assertEquals(1, report.getTotalEvaluations());
        assertEquals(1, report.getCompletedEvaluations());
        assertEquals(0, report.getIncompleteEvaluations());
        assertEquals(1, report.getGroups().size());
    }
}