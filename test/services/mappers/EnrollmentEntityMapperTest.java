package services.mappers;

import models.Course;
import models.Enrollment;
import models.User;
import models.dto.Context;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import repository.core.CourseRepository;
import repository.core.UserRepository;
import services.processors.record.InputRecord;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class EnrollmentEntityMapperTest {

    private EnrollmentEntityMapper enrollmentEntityMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private InputRecord csvRecord;

    @Mock
    private User mockStudent;

    @Mock
    private Course mockCourse;

    @Mock
    private Context context;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        enrollmentEntityMapper = new EnrollmentEntityMapper(userRepository, courseRepository);
    }

    /** Test Mapping When Student and Course Exist **/
    @Test
    public void testMapToEntityShouldReturnEnrollmentWhenStudentAndCourseExist() {
        when(csvRecord.get("student_id")).thenReturn(String.valueOf(123L));
        when(csvRecord.get("course_code")).thenReturn("CS101");
        when(csvRecord.get("course_section")).thenReturn("SS");
        when(csvRecord.get("term")).thenReturn("Fall 2024");

        when(userRepository.findById(123L)).thenReturn(Optional.of(mockStudent));
        when(courseRepository.findByCourseCodeAndSectionAndTerm("CS101", "SS", "Fall 2024")).thenReturn(Optional.of(mockCourse));

        Enrollment enrollment = enrollmentEntityMapper.mapToEntity(csvRecord, context);

        assertNotNull(enrollment);
        assertEquals(mockStudent, enrollment.getStudent());
        assertEquals(mockCourse, enrollment.getCourse());
    }

    /** Test Mapping When Student Does Not Exist **/
    @Test
    public void testMapToEntityShouldReturnEnrollmentWithNullStudentWhenStudentDoesNotExist() {
        when(csvRecord.get("student_id")).thenReturn(String.valueOf(999L));
        when(csvRecord.get("course_code")).thenReturn("CS101");
        when(csvRecord.get("course_section")).thenReturn("SS");
        when(csvRecord.get("term")).thenReturn("Fall 2024");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        when(courseRepository.findByCourseCodeAndSectionAndTerm("CS101", "SS", "Fall 2024")).thenReturn(Optional.of(mockCourse));

        Enrollment enrollment = enrollmentEntityMapper.mapToEntity(csvRecord,context );

        assertNotNull(enrollment);
        assertNull(enrollment.getStudent());
        assertEquals(mockCourse, enrollment.getCourse());
    }

    /** Test Mapping When Course Does Not Exist **/
    @Test
    public void testMapToEntityShouldReturnEnrollmentWithNullCourseWhenCourseDoesNotExist() {
        when(csvRecord.get("student_id")).thenReturn(String.valueOf(123L));
        when(csvRecord.get("course_code")).thenReturn("CS999");
        when(csvRecord.get("course_section")).thenReturn("SS");
        when(csvRecord.get("term")).thenReturn("Fall 2024");

        when(userRepository.findById(123L)).thenReturn(Optional.of(mockStudent));
        when(courseRepository.findByCourseCodeAndSectionAndTerm("CS999", "SS", "Fall 2024")).thenReturn(Optional.empty());

        Enrollment enrollment = enrollmentEntityMapper.mapToEntity(csvRecord, context);

        assertNotNull(enrollment);
        assertEquals(mockStudent, enrollment.getStudent());
        assertNull(enrollment.getCourse());
    }

    /** Test Mapping When Both Student and Course Do Not Exist **/
    @Test
    public void testMapToEntityShouldReturnEnrollmentWithNullStudentAndNullCourseWhenBothDoNotExist() {
        when(csvRecord.get("student_id")).thenReturn(String.valueOf(999L));
        when(csvRecord.get("course_code")).thenReturn("CS999");
        when(csvRecord.get("course_section")).thenReturn("SS");
        when(csvRecord.get("term")).thenReturn("Fall 2024");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        when(courseRepository.findByCourseCodeAndSectionAndTerm("CS999", "SS", "Fall 2024")).thenReturn(Optional.empty());

        Enrollment enrollment = enrollmentEntityMapper.mapToEntity(csvRecord,context );

        assertNotNull(enrollment);
        assertNull(enrollment.getStudent());
        assertNull(enrollment.getCourse());
    }
}