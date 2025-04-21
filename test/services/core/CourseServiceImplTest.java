package services.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import models.Course;
import org.junit.Before;
import org.junit.Test;
import repository.core.CourseRepository;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CourseServiceImplTest {

    private CourseRepository courseRepository;
    private CourseServiceImpl courseService;

    @Before
    public void setUp() {
        courseRepository = mock(CourseRepository.class);
        courseService = new CourseServiceImpl(courseRepository);
    }

    @Test
    public void testGetActiveCoursesByProfessorId_Success() {
        when(courseRepository.findActiveCoursesByProfessorId(1L, "CS101", "A", "Fall 2025"))
                .thenReturn(CompletableFuture.completedFuture(2));

        CompletableFuture<Integer> result = courseService.getActiveCoursesByProfessorId(1L, "CS101", "A", "Fall 2025");

        assertEquals(2, result.join());
        verify(courseRepository).findActiveCoursesByProfessorId(1L, "CS101", "A", "Fall 2025");
    }

    @Test
    public void testGetAllCourses_Success() {
        List<Map<String, String>> mockData = List.of(Map.of("courseCode", "CS101"));
        when(courseRepository.findAllCourses(1L, "Fall 2025"))
                .thenReturn(CompletableFuture.completedFuture(mockData));

        CompletableFuture<List<Map<String, String>>> result = courseService.getAllCourses(1L, "Fall 2025");

        assertEquals(1, result.join().size());
        assertEquals("CS101", result.join().get(0).get("courseCode"));
    }

    @Test
    public void testGetAllTerms_AddsMissingTerms() {
        when(courseRepository.findAllTerms(1L))
                .thenReturn(CompletableFuture.completedFuture(List.of("Winter 2025")));

        List<String> result = courseService.getAllTerms(1L).join();

        assertTrue(result.contains("Winter 2025"));
        assertTrue(result.stream().anyMatch(term -> term.startsWith("Fall ")));
        assertTrue(result.size() > 1);
    }

    @Test
    public void testUnassignCourse_Success() {
        Course course = new Course();
        course.setCourseId(42L);

        when(courseRepository.findByCourseCodeAndSectionAndTerm("CS101", "A", "Fall 2025"))
                .thenReturn(Optional.of(course));

        boolean result = courseService.unassignCourse("CS101:::A:::Fall 2025").toCompletableFuture().join();

        assertTrue(result);
        verify(courseRepository).unassignCourse(42L);
    }

    @Test
    public void testUnassignCourse_NotFound() {
        when(courseRepository.findByCourseCodeAndSectionAndTerm("CS101", "A", "Fall 2025"))
                .thenReturn(Optional.empty());

        boolean result = courseService.unassignCourse("CS101:::A:::Fall 2025").toCompletableFuture().join();

        assertFalse(result);
        verify(courseRepository, never()).unassignCourse(any());
    }
}