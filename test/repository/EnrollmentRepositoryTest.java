package repository;

import models.Course;
import models.Enrollment;
import models.User;
import models.dto.Context;
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
    private Context mockContext;

    @Mock
    private EntityManager mockEntityManager;

    private EnrollmentRepository enrollmentRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

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

        // Execute the method
        CompletionStage<Map<String, Object>> resultStage = enrollmentRepository.saveAll(enrollments, mockContext);
        Map<String, Object> result = resultStage.toCompletableFuture().join();

        // Assert the results
        assertNotNull(result);
        assertEquals(2, result.get("successCount"));
        assertEquals(0, result.get("failedCount"));
        assertEquals(Collections.emptyList(), result.get("failedRecords"));
    }
}