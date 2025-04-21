package services.core;

import forms.AssignmentForm;
import models.*;
import models.dto.AssignmentEditDTO;
import models.enums.Status;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import repository.core.AssignmentRepository;
import repository.core.CourseRepository;
import repository.core.FeedbackRepository;
import services.validations.AssignmentFormValidation;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AssignmentServiceImplTest {

    @Mock private AssignmentRepository assignmentRepository;
    @Mock private AssignmentFormValidation assignmentFormValidation;
    @Mock private CourseRepository courseRepository;
    @Mock private EnrollmentService enrollmentService;
    @Mock private FeedbackRepository feedbackRepository;

    @InjectMocks private AssignmentServiceImpl assignmentService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        assignmentService = new AssignmentServiceImpl(
                assignmentRepository,
                assignmentFormValidation,
                courseRepository,
                enrollmentService,
                feedbackRepository
        );
    }

    @Test
    public void testGetAssignmentDetails_Success() {
        Long assignmentId = 1L;
        Assignment assignment = new Assignment();
        assignment.setAssignmentId(assignmentId);
        assignment.setTitle("Assignment");
        assignment.setDescription("Description");
        assignment.setStartDate(LocalDate.now().minusDays(2));
        assignment.setDueDate(LocalDate.now().plusDays(2));

        Course course = new Course();
        course.setCourseCode("CS101");
        course.setCourseSection("001");
        course.setTerm("Fall 2024");
        assignment.setCourse(course);

        FeedbackQuestion question = new FeedbackQuestion();
        question.setQuestionId(1L);
        question.setQuestionText("Q1");
        question.setMaxMarks(10);
        assignment.setFeedbackQuestions(Collections.singletonList(question));

        when(assignmentRepository.findByIdWithFeedbackQuestions(assignmentId)).thenReturn(Optional.of(assignment));

        AssignmentEditDTO dto = assignmentService.getAssignmentDetails(assignmentId).join();
        assertNotNull(dto);
        assertEquals("Assignment", dto.getTitle());
        assertEquals(1, dto.reviewQuestions.size());
        assertEquals("Q1", dto.reviewQuestions.get(0).question);
    }

    @Test(expected = CompletionException.class)
    public void testGetAssignmentDetails_NotFound() {
        when(assignmentRepository.findByIdWithFeedbackQuestions(anyLong())).thenReturn(Optional.empty());
        assignmentService.getAssignmentDetails(1L).join();
    }

    @Test
    public void testDeleteAssignment_Success() {
        Long assignmentId = 1L;
        Assignment assignment = new Assignment();
        assignment.setAssignmentId(assignmentId);

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.deleteAssignmentById(assignmentId)).thenReturn(CompletableFuture.completedFuture(true));

        Boolean result = assignmentService.deleteAssignment(assignmentId).join();
        assertTrue(result);
    }

    @Test
    public void testDeleteAssignment_NotFound() {
        when(assignmentRepository.findById(anyLong())).thenReturn(Optional.empty());
        Boolean result = assignmentService.deleteAssignment(1L).join();
        assertFalse(result);
    }

    @Test
    public void testCreateAssignment_Success() {
        AssignmentForm form = new AssignmentForm();
        form.setCourseCode("CS101");
        form.setCourseSection("001");
        form.setTerm("Fall 2024");
        form.setTitle("New Assignment");
        form.setDescription("Test");
        form.setStartDate(LocalDate.now().minusDays(1));
        form.setDueDate(LocalDate.now().plusDays(1));
        form.getQuestions().add(new AssignmentForm.ReviewQuestionForm(null, "Clarity", 5));

        Course course = new Course();
        course.setStudentFileUploaded(true);
        when(courseRepository.findByCourseCodeAndSectionAndTerm(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(course));
        when(assignmentFormValidation.isValid(form)).thenReturn(true);

        Result result = assignmentService.createAssignment(form).join();
        assertEquals(200, result.status());
    }

    @Test
    public void testCreateAssignment_InvalidForm() {
        AssignmentForm form = new AssignmentForm();
        when(assignmentFormValidation.isValid(form)).thenReturn(false);

        Result result = assignmentService.createAssignment(form).join();
        assertEquals(400, result.status());
    }

    @Test
    public void testCreateAssignment_CourseNotFound() {
        AssignmentForm form = new AssignmentForm();
        form.setCourseCode("CS101");
        form.setCourseSection("001");
        form.setTerm("Fall 2024");
        when(courseRepository.findByCourseCodeAndSectionAndTerm(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(assignmentFormValidation.isValid(form)).thenReturn(true);

        Result result = assignmentService.createAssignment(form).join();
        assertEquals(400, result.status());
    }
}