package repository;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import models.Assignment;
import models.Course;
import models.ReviewTask;
import models.User;
import models.dto.PeerReviewSummaryDTO;
import models.enums.Status;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.db.jpa.JPAApi;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class DashboardRepositoryTest {

    @Mock private JPAApi mockJPAApi;
    @Mock private EntityManager mockEntityManager;
    @Mock private TypedQuery<Assignment> mockAssignmentQuery;
    @Mock private TypedQuery<Long> mockCountQuery;
    @Mock private TypedQuery<ReviewTask> mockReviewTaskQuery;

    private DashboardRepository dashboardRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        dashboardRepository = new DashboardRepository(mockJPAApi);

        // Mock JPA transaction
        when(mockJPAApi.withTransaction(any(Function.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    Function<EntityManager, Object> func = (Function<EntityManager, Object>) invocation.getArgument(0);
                    return func.apply(mockEntityManager);
                });
    }

    @Test
    public void testGetAssignmentSummaryForProfessor_returnsAssignments() throws Exception {
        Long professorId = 101L;

        Assignment assignment = new Assignment();
        assignment.setTitle("Assignment 1");

        Course mockCourse = new Course();
        mockCourse.setCourseCode("CS101");
        mockCourse.setCourseSection("A");
        mockCourse.setTerm("Fall 2025");

        // Inject professor using reflection
        setField(mockCourse, "professor", new User(professorId, "prof@example.com", "", "Prof", "X", "professor"));

        assignment.setCourse(mockCourse);
        assignment.setStartDate(LocalDate.now().minusDays(1));
        assignment.setDueDate(LocalDate.now().plusDays(7));
        assignment.setPeerAssigned(true);

        when(mockEntityManager.createQuery(anyString(), eq(Assignment.class))).thenReturn(mockAssignmentQuery);
        when(mockAssignmentQuery.setParameter(anyString(), any())).thenReturn(mockAssignmentQuery);
        when(mockAssignmentQuery.getResultList()).thenReturn(List.of(assignment));

        CompletableFuture<List<Assignment>> resultFuture =
                dashboardRepository.getAssignmentSummaryForProfessor(professorId, null, null, null);
        List<Assignment> results = resultFuture.get();

        assertEquals(1, results.size());
        assertEquals("Assignment 1", results.get(0).getTitle());
    }

    @Test
    public void testGetPeerReviewProgressForProfessor_returnsSummary() throws Exception {
        Long professorId = 101L;

        Assignment assignment = new Assignment();
        assignment.setAssignmentId(1L);
        assignment.setTitle("Peer Assignment");

        Course course = new Course();
        course.setCourseCode("CS101");
        course.setCourseSection("A");
        course.setTerm("Fall 2025");
        course.setCourseId(501L);

        // Inject professor using reflection
        setField(course, "professor", new User(professorId, "prof@example.com", "", "Prof", "X", "professor"));
        assignment.setCourse(course);

        User reviewer = new User(100L, "stud1@uni.com", "", "Stud", "1", "student");

        ReviewTask reviewTask = new ReviewTask(
                assignment,
                reviewer,
                new User(200L, "reviewee@uni.com", "", "Peer", "1", "student"),
                Status.COMPLETED,
                1L,
                "Group A",
                3,
                false
        );

        when(mockEntityManager.createQuery(startsWith("SELECT a FROM Assignment"), eq(Assignment.class)))
                .thenReturn(mockAssignmentQuery);
        when(mockAssignmentQuery.setParameter(anyString(), any())).thenReturn(mockAssignmentQuery);
        when(mockAssignmentQuery.getResultList()).thenReturn(List.of(assignment));

        when(mockEntityManager.createQuery(startsWith("SELECT r FROM ReviewTask"), eq(ReviewTask.class)))
                .thenReturn(mockReviewTaskQuery);
        when(mockReviewTaskQuery.setParameter(eq("assignmentId"), any())).thenReturn(mockReviewTaskQuery);
        when(mockReviewTaskQuery.getResultList()).thenReturn(List.of(reviewTask));

        when(mockEntityManager.createQuery(startsWith("SELECT COUNT"), eq(Long.class))).thenReturn(mockCountQuery);
        when(mockCountQuery.setParameter(eq("courseId"), eq(501L))).thenReturn(mockCountQuery);
        when(mockCountQuery.getSingleResult()).thenReturn(10L);

        CompletableFuture<List<PeerReviewSummaryDTO>> resultFuture =
                dashboardRepository.getPeerReviewProgressForProfessor(professorId, null, null, null);

        List<PeerReviewSummaryDTO> result = resultFuture.get();

        assertEquals(1, result.size());
    }

    // Utility method to inject private field
    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}