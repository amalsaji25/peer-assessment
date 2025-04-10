package services.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import models.*;
import models.dto.FeedbacksReceivedDTO;
import models.enums.Status;
import org.junit.Before;
import org.junit.Test;
import repository.core.FeedbackRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class FeedbackServiceImplTest {

    private FeedbackRepository feedbackRepository;
    private EnrollmentService enrollmentService;
    private FeedbackServiceImpl feedbackService;

    @Before
    public void setUp() {
        feedbackRepository = mock(FeedbackRepository.class);
        enrollmentService = mock(EnrollmentService.class);
        feedbackService = new FeedbackServiceImpl(feedbackRepository, enrollmentService);
    }

    @Test
    public void testGetFeedbacksForReviewTaskId_ReturnsFeedbackList() {
        Long reviewTaskId = 1L;

        Feedback feedback = mock(Feedback.class);
        List<Feedback> feedbackList = List.of(feedback);

        when(feedbackRepository.findFeedbacksReviewTaskId(reviewTaskId))
                .thenReturn(Optional.of(feedbackList));

        Optional<List<Feedback>> result = feedbackService.getFeedbacksForReviewTaskId(reviewTaskId);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        verify(feedbackRepository).findFeedbacksReviewTaskId(reviewTaskId);
    }

    @Test
    public void testGetFeedbacksReceivedByStudent_ReturnsDTOs() {
        Long userId = 1L;
        String courseCode = "CS101";

        // Mock course enrollment
        when(enrollmentService.findStudentEnrolledCourseCodes(userId, courseCode))
                .thenReturn(CompletableFuture.completedFuture(List.of("CS101")));

        // Mock assignment
        Assignment assignment = mock(Assignment.class);
        when(assignment.getTitle()).thenReturn("Assignment 1");
        when(assignment.getAssignmentId()).thenReturn(10L);
        when(assignment.getDueDate()).thenReturn(LocalDate.now().minusDays(1));

        // Mock review task
        ReviewTask rt = mock(ReviewTask.class);
        when(rt.getStatus()).thenReturn(Status.COMPLETED);
        when(rt.getAssignment()).thenReturn(assignment);

        // Mock feedback question
        FeedbackQuestion question = mock(FeedbackQuestion.class);
        when(question.getQuestionText()).thenReturn("Q1");
        when(question.getMaxMarks()).thenReturn(10);

        // Mock feedback
        Feedback feedback = mock(Feedback.class);
        when(feedback.getReviewTask()).thenReturn(rt);
        when(feedback.getQuestion()).thenReturn(question);
        when(feedback.getScore()).thenReturn(7);

        List<Feedback> feedbacks = List.of(feedback, feedback); // two for same question

        when(feedbackRepository.findFeedbacksReceivedByStudent(userId, List.of("CS101")))
                .thenReturn(CompletableFuture.completedFuture(feedbacks));

        List<FeedbacksReceivedDTO> result =
                feedbackService.getFeedbacksReceivedByStudent(userId, courseCode).join();

        assertEquals(1, result.size());
        FeedbacksReceivedDTO dto = result.get(0);
        assertEquals("Assignment 1", dto.getAssignmentTitle());
        assertEquals(10, dto.getTotalMarks());
        assertEquals(7, dto.getObtainedMarks()); // average of [7,7] = 7

        verify(feedbackRepository).findFeedbacksReceivedByStudent(userId, List.of("CS101"));
    }
}