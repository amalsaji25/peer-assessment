package repository;

import jakarta.persistence.TypedQuery;
import models.Course;
import models.Enrollment;
import models.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import play.db.jpa.JPAApi;

import jakarta.persistence.EntityManager;
import repository.core.EnrollmentRepository;

import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EnrollmentRepositoryTest {

    @Mock
    private JPAApi mockJPAApi;

    @Mock
    private EntityManager mockEntityManager;

    private EnrollmentRepository enrollmentRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(mockJPAApi.withTransaction(any(Function.class)))
                .thenAnswer(invocation -> {
                    Function<EntityManager, Object> function = invocation.getArgument(0);
                    return function.apply(mockEntityManager);
                });

        enrollmentRepository = new EnrollmentRepository(mockJPAApi);
    }

    /**
     * Test bulk save with successful enrollments
     */
    @Test
    public void testSaveAllShouldSaveEnrollmentsSuccessfully() {
        // Create student and professor entities
        User student1 = new User(10000L, "student1@example.com","", "First", "Student", "student");
        User student2 = new User(10001L, "student2@example.com","", "Second", "Student", "student");
        User professor = new User(10002L, "prof@example.com","", "Prof", "Smith", "professor");

        Course course = new Course("CS101", "Computer Science", professor, "Fall 2024", "SS", false);

        Enrollment enrollment1 = new Enrollment(student1, course, "SS", "Fall 2024");
        Enrollment enrollment2 = new Enrollment(student2, course, "SS", "Fall 2024");

        List<Enrollment> enrollments = Arrays.asList(enrollment1, enrollment2);

        // Mock query chain to simulate no duplicates
        TypedQuery<Enrollment> mockQuery = mock(TypedQuery.class);

        when(mockEntityManager.createQuery(anyString(), eq(Enrollment.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.getResultStream()).thenAnswer(invocation -> Stream.empty());

        // Execute
        CompletionStage<Map<String, Object>> resultStage = enrollmentRepository.saveAll(enrollments);
        Map<String, Object> result = resultStage.toCompletableFuture().join();

        // Verify
        assertEquals(2, result.get("successCount"));
        assertEquals(0, result.get("skippedCount"));
        assertEquals(0, result.get("failedCount"));
    }
}