package repository;

import models.Course;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import play.db.jpa.JPAApi;

import jakarta.persistence.EntityManager;
import repository.core.CourseRepository;

import java.util.*;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CourseRepositoryTest {

    @Mock
    private JPAApi mockJPAApi;

    @Mock
    private EntityManager mockEntityManager;

    private CourseRepository courseRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(mockJPAApi.withTransaction(any(Function.class)))
                .then(invocation -> {
                    Function<EntityManager, Object> function = invocation.getArgument(0);
                    return function.apply(mockEntityManager);
                });

        courseRepository = new CourseRepository(mockJPAApi);
    }

    /** Test that findByCourseCode returns course when found **/
    @Test
    public void testFindByCourseCodeShouldReturnCourseIfExists() {
        Course expectedCourse = new Course("CS101", "Computer Science", null);

        when(mockEntityManager.find(Course.class, "CS101")).thenReturn(expectedCourse);

        Optional<Course> result = courseRepository.findByCourseCode("CS101");

        assertTrue(result.isPresent());
        assertEquals("CS101", result.get().getCourseCode());
    }

    /** Test that findByCourseCode returns empty if course does not exist **/
    @Test
    public void testFindByCourseCodeShouldReturnEmptyIfNotExists() {
        when(mockEntityManager.find(Course.class, "CS999")).thenReturn(null);

        Optional<Course> result = courseRepository.findByCourseCode("CS999");

        assertFalse(result.isPresent());
    }

    /** Test that saveAll correctly batches and reports success/failures **/
    @Test
    public void testSaveAllShouldReturnCorrectSuccessAndFailureCounts() {
        List<Course> cours = Arrays.asList(
                new Course("CS101", "Computer Science", null),
                new Course("CS102", "Data Structures", null),
                new Course("CS103", "Algorithms", null)
        );

        doNothing().when(mockEntityManager).persist(any(Course.class));

        CompletionStage<Map<String, Object>> resultStage = courseRepository.saveAll(cours);
        Map<String, Object> result = resultStage.toCompletableFuture().join();

        assertEquals(3, result.get("successCount"));
        assertEquals(0, result.get("failedCount"));
    }
}