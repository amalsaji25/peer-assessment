package repository;

import models.Courses;
import models.Enrollments;
import models.Users;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import play.db.jpa.JPAApi;

import jakarta.persistence.EntityManager;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

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
        Users mockStudent = mock(Users.class);
        Users mockStudent1 = mock(Users.class);
        Users mockProfessor = mock(Users.class);
        Courses course = new Courses("CS101", "Computer Science", mockProfessor);

        List<Enrollments> enrollments = Arrays.asList(
                new Enrollments(mockStudent, course),
                new Enrollments(mockStudent1, course)
        );

        // Simulate persistence success
        doNothing().when(mockEntityManager).persist(any(Enrollments.class));

        // Execute the method
        CompletionStage<Map<String, Object>> resultStage = enrollmentRepository.saveAll(enrollments);
        Map<String, Object> result = resultStage.toCompletableFuture().join();

        // Assertions
        assertEquals(2, result.get("successCount"));
        assertEquals(0, result.get("failedCount"));
    }

    /**
     * Test bulk save with one enrollment failing
     */
    @Test
    public void testSaveAllShouldHandleEnrollmentFailures() {
        Users mockStudent = mock(Users.class);
        Users mockStudent1 = mock(Users.class);
        Users mockProfessor = mock(Users.class);
        Courses course = new Courses("CS101", "Computer Science", mockProfessor);

        when(mockStudent1.getUserId()).thenReturn("U102");

        Enrollments enrollment1 = new Enrollments(mockStudent, course);
        Enrollments enrollment2 = new Enrollments(mockStudent1, course);

        List<Enrollments> enrollments = Arrays.asList(enrollment1, enrollment2);

        // Simulate persistence success for the first, failure for the second
        doNothing().when(mockEntityManager).persist(enrollment1);
        doThrow(new RuntimeException("DB Error")).when(mockEntityManager).persist(enrollment2);

        // Execute the method
        CompletionStage<Map<String, Object>> resultStage = enrollmentRepository.saveAll(enrollments);
        Map<String, Object> result = resultStage.toCompletableFuture().join();

        // Assertions
        assertEquals(1, result.get("successCount"));
        assertEquals(1, result.get("failedCount"));
        assertTrue(((List<String>) result.get("failedRecords"))
                .contains("Student: U102, Course: CS101"));
    }
}