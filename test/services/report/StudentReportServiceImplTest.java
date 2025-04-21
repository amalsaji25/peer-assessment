package services.report;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import models.Assignment;
import models.Course;
import models.ReviewTask;
import models.User;
import models.dto.*;
import models.enums.Status;
import org.junit.Before;
import org.junit.Test;
import services.core.ReviewTaskService;

public class StudentReportServiceImplTest {

    private ReviewTaskService reviewTaskService;
    private StudentReportServiceImpl studentReportService;

    @Before
    public void setUp() {
        reviewTaskService = mock(ReviewTaskService.class);
        studentReportService = new StudentReportServiceImpl(reviewTaskService);
    }

    @Test
    public void testGenerateReport_ValidData_SuccessfullyGeneratesReport() {
        // Setup Course and Assignment
        Course course = new Course();
        course.setCourseCode("CS101");
        course.setCourseName("Intro to CS");
        course.setCourseSection("A");
        course.setTerm("Fall 2025");

        Assignment assignment = new Assignment();
        assignment.setAssignmentId(1L);
        assignment.setTitle("Assignment 1");
        assignment.setCourse(course);

        // User setup
        Long reviewerId = 10L;
        Long studentId = 20L;

        User reviewer = new User(reviewerId, "peer@example.com", "", "Peer", "One", "student");
        User student = new User(studentId, "student@example.com", "", "Student", "A", "student");

        // ReviewTask
        ReviewTask reviewTask = new ReviewTask(assignment, reviewer, student, Status.COMPLETED, 101L, "Group 1", 2, false);
        List<ReviewTask> reviewTasks = List.of(reviewTask);

        when(reviewTaskService.getReviewTasks(1L)).thenReturn(CompletableFuture.completedFuture(reviewTasks));

        // Grouping
        Map<Long, List<ReviewTask>> grouped = new HashMap<>();
        grouped.put(101L, reviewTasks);
        when(reviewTaskService.groupReviewTasksByGroup(reviewTasks)).thenReturn(grouped);

        // FeedbackDTO using constructor only
        FeedbackDTO feedbackDTO = new FeedbackDTO(100L, 8, 10, "Q1", "Great job!");
        Map<Long, List<FeedbackDTO>> feedbacksByReviewer = new HashMap<>();
        feedbacksByReviewer.put(reviewerId, List.of(feedbackDTO));

        // Member DTO
        MemberSubmissionDTO member = new MemberSubmissionDTO();
        member.setUserId(studentId);
        member.setUserName("Student A");
        member.setAverageFeedbackScore(8.0f);
        member.setFeedbacksByReviewer(feedbacksByReviewer);
        member.setClassAveragesForEachQuestion(Map.of("Q1", 7.5f));
        member.setOverallClassAverage(7.5f);

        // Group DTO
        GroupSubmissionDTO group = new GroupSubmissionDTO();
        group.setGroupId(101L);
        group.setGroupName("Group 1");
        group.setMembers(List.of(member));

        when(reviewTaskService.generateSubmissionInfoInEachGroupDTOs(grouped)).thenReturn(List.of(group));
        doNothing().when(reviewTaskService).calculateClassAverages(anyList());

        // Act
        ReportDTO report = studentReportService.generateReport(1L, studentId).join();

        // Assert
        assertNotNull(report);
        assertEquals((Long) studentId, report.getUserId());
        assertEquals("Student A", report.getUserName());
        assertEquals("Group 1", report.getGroupName());
        assertEquals("Assignment 1", report.getAssignmentTitle());
        assertEquals("CS101", report.getCourseCode());
        assertEquals("Intro to CS", report.getCourseName());
        assertEquals("A", report.getCourseSection());
        assertEquals("Fall 2025", report.getTerm());
        assertEquals(7.5f, report.getOverallClassAverage(), 0.01f);
        assertEquals(8.0f, report.getGetStudentAverageScore(), 0.01f);
        assertTrue(report.getGroupedAnonymizedFeedbacks().containsKey(reviewerId));
        assertEquals(1, report.getGroupedAnonymizedFeedbacks().get(reviewerId).size());

        FeedbackDTO anonFeedback = report.getGroupedAnonymizedFeedbacks().get(reviewerId).get(0);
        assertEquals("Great job!", anonFeedback.getFeedbackText());
        assertEquals("Q1", anonFeedback.getQuestionText());
        assertTrue(anonFeedback.getReviewerName().startsWith("Peer "));
    }
}