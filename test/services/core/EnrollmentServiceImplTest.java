package services.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import repository.core.EnrollmentRepository;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class EnrollmentServiceImplTest {

    private EnrollmentRepository enrollmentRepository;
    private EnrollmentServiceImpl enrollmentService;

    @Before
    public void setUp() {
        enrollmentRepository = mock(EnrollmentRepository.class);
        enrollmentService = new EnrollmentServiceImpl(enrollmentRepository);
    }

    @Test
    public void testGetStudentCountByProfessorId_Success() {
        when(enrollmentRepository.getStudentCountByProfessorId(1L, "CS101", "A", "Winter 2025"))
                .thenReturn(CompletableFuture.completedFuture(42));

        int count = enrollmentService
                .getStudentCountByProfessorId(1L, "CS101", "A", "Winter 2025")
                .join();

        assertEquals(42, count);
        verify(enrollmentRepository).getStudentCountByProfessorId(1L, "CS101", "A", "Winter 2025");
    }

    @Test
    public void testFindStudentEnrolledCourseCodes_All_ReturnsAllCourses() {
        List<String> mockCourses = List.of("CS101", "CS102");
        when(enrollmentRepository.findCourseCodesByStudentId(2L))
                .thenReturn(CompletableFuture.completedFuture(mockCourses));

        List<String> result = enrollmentService.findStudentEnrolledCourseCodes(2L, "all").join();

        assertEquals(2, result.size());
        assertTrue(result.contains("CS101"));
    }

    @Test
    public void testFindStudentEnrolledCourseCodes_EnrolledInCourse_ReturnsCourse() {
        when(enrollmentRepository.isStudentEnrolledInCourse(3L, "CS105"))
                .thenReturn(CompletableFuture.completedFuture(true));

        List<String> result = enrollmentService.findStudentEnrolledCourseCodes(3L, "CS105").join();

        assertEquals(List.of("CS105"), result);
    }

    @Test
    public void testFindStudentEnrolledCourseCodes_NotEnrolledInCourse_ReturnsEmptyList() {
        when(enrollmentRepository.isStudentEnrolledInCourse(3L, "CS105"))
                .thenReturn(CompletableFuture.completedFuture(false));

        List<String> result = enrollmentService.findStudentEnrolledCourseCodes(3L, "CS105").join();

        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetStudentEnrolledCourse_ReturnsCourses() {
        List<Map<String, String>> courses = List.of(
                Map.of("courseCode", "CS101"),
                Map.of("courseCode", "CS102")
        );

        when(enrollmentRepository.findEnrolledCoursesForStudentId(4L))
                .thenReturn(CompletableFuture.completedFuture(courses));

        List<Map<String, String>> result = enrollmentService.getStudentEnrolledCourse(4L).join();

        assertEquals(2, result.size());
        assertEquals("CS101", result.get(0).get("courseCode"));
    }
}