package services.core;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.*;
import models.dto.*;
import models.enums.Status;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import repository.core.ReviewTaskRepository;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ReviewTaskServiceImplTest {

    @Mock
    private ReviewTaskRepository reviewTaskRepository;

    @Mock
    private FeedbackService feedbackService;

    @InjectMocks
    private ReviewTaskServiceImpl reviewTaskService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetReviewTasksSubmissionOverview_ReturnsCorrectDTO() {
        Long assignmentId = 1L;

        Assignment assignment = new Assignment();
        User reviewer = new User(1L, "John");
        User reviewee = new User(2L, "Jane");

        ReviewTask task = new ReviewTask(
                assignment,
                reviewer,
                reviewee,
                Status.COMPLETED,
                101L,
                "Alpha",
                2,
                false
        );

        Feedback feedback = new Feedback();
        feedback.setReviewTask(task);
        feedback.setScore(10);
        feedback.setFeedbackText("Good job");
        FeedbackQuestion question = new FeedbackQuestion();
        question.setQuestionText("Clarity");
        question.setMaxMarks(10);
        feedback.setQuestion(question);

        when(reviewTaskRepository.findByAssignmentId(assignmentId))
                .thenReturn(Optional.of(List.of(task)));

        when(feedbackService.getFeedbacksForReviewTaskId(100L))
                .thenReturn(Optional.of(List.of(feedback)));

        CompletableFuture<SubmissionOverviewDTO> future =
                reviewTaskService.getReviewTasksSubmissionOverview(assignmentId);

        SubmissionOverviewDTO result = future.join();

        assertNotNull(result);
        assertEquals(1, result.getTotalSubmissions());
        assertEquals(100, result.getReviewsCompleted()); // 1 of 1 completed
    }

    @Test
    public void testGetReviewCountByStatus_WithCourse() {
        when(reviewTaskRepository.findReviewCountByStudentIdAndStatusForCourse(1L, "CS101", Status.PENDING))
                .thenReturn(CompletableFuture.completedFuture(3));

        int count = reviewTaskService.getReviewCountByStatus(1L, "CS101", Status.PENDING).join();
        assertEquals(3, count);
    }

    @Test
    public void testParseAndSaveOrSubmitReviewTask_Success() throws Exception {
        Long taskId = 1L;
        String jsonStr = """
                {
                  "status": "COMPLETED",
                  "feedbacks": [
                    {
                      "feedbackId": 1,
                      "marks": 8,
                      "feedback": "Nice work"
                    }
                  ]
                }
                """;

        JsonNode json = objectMapper.readTree(jsonStr);

        String result = reviewTaskService.parseAndSaveOrSubmitReviewTask(taskId, json).join();
        assertEquals("Review completed successfully", result);

        verify(reviewTaskRepository, times(1)).saveReviewTaskFeedback(any(ReviewTaskDTO.class));
    }
}