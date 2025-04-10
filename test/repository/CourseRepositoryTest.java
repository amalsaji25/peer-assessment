package repository;

import jakarta.persistence.TypedQuery;
import models.Course;
import models.dto.Context;
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
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CourseRepositoryTest {

    @Mock
    private JPAApi mockJPAApi;

    @Mock
    private Context mockContext;

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

    /** Test that findByCourseCodeAndSectionAndTerm returns course when found **/
    @Test
    public void testFindByCourseCodeShouldReturnCourseIfExistsAndTerm() {
        Course expectedCourse = new Course("CS101", "Computer Science", null, "Fall 2024", "SS", false);

        TypedQuery<Course> mockQuery = mock(TypedQuery.class);

        when(mockEntityManager.createQuery(anyString(), eq(Course.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter(eq("courseCode"), eq("CS101"))).thenReturn(mockQuery);
        when(mockQuery.setParameter(eq("term"), eq("Fall 2024"))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(List.of(expectedCourse));

        Optional<Course> result = courseRepository.findByCourseCodeAndSectionAndTerm("CS101", "SS", "Fall 2024");

        assertTrue(result.isPresent());
        assertEquals("CS101", result.get().getCourseCode());
    }

    /** Test that findByCourseCodeAndSectionAndTerm returns empty if course does not exist **/
    @Test
    public void testFindByCourseCodeAndSectionAndTermShouldReturnEmptyIfNotExists() {
        TypedQuery<Course> mockQuery = mock(TypedQuery.class);

        when(mockEntityManager.createQuery(anyString(), eq(Course.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter(eq("courseCode"), eq("CS999"))).thenReturn(mockQuery);
        when(mockQuery.setParameter(eq("term"), eq("Fall 2024"))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(Collections.emptyList()); // No match found

        Optional<Course> result = courseRepository.findByCourseCodeAndSectionAndTerm("CS999", "SS", "Fall 2024");

        assertFalse(result.isPresent());
    }

    /** Test that saveAll correctly batches and reports success/failures **/
    @Test
    public void testSaveAllShouldReturnCorrectSuccessAndFailureCounts() {
        List<Course> cours = Arrays.asList(
                new Course("CS101", "Computer Science", null, "Fall 2024", "SS", false),
                new Course("CS102", "Data Structures", null, "Fall 2024", "SS", false),
                new Course("CS103", "Algorithms", null, "Fall 2024", "SS", false)
        );

        // Mock the query that checks for existing courses
        TypedQuery<Course> mockQuery = mock(TypedQuery.class);
        when(mockEntityManager.createQuery(anyString(), eq(Course.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.getResultStream()).thenAnswer(invocation -> Stream.empty()); // simulate no existing courses

        // Mock persist
        doNothing().when(mockEntityManager).persist(any(Course.class));

        CompletionStage<Map<String, Object>> resultStage = courseRepository.saveAll(cours, mockContext);
        Map<String, Object> result = resultStage.toCompletableFuture().join();

        assertEquals(3, result.get("successCount"));
        assertEquals(0, result.get("failedCount"));
    }
}