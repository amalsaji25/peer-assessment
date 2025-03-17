package services.mappers;

import models.Courses;
import models.Enrollments;
import models.Users;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import repository.CourseRepository;
import repository.UserRepository;

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
    private CSVRecord csvRecord;

    @Mock
    private Users mockStudent;

    @Mock
    private Courses mockCourse;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        enrollmentEntityMapper = new EnrollmentEntityMapper(userRepository, courseRepository);
    }

    /** Test Mapping When Student and Course Exist **/
    @Test
    public void testMapToEntityShouldReturnEnrollmentWhenStudentAndCourseExist() {
        when(csvRecord.get("student_id")).thenReturn("S123");
        when(csvRecord.get("course_id")).thenReturn("CS101");

        when(userRepository.findById("S123")).thenReturn(Optional.of(mockStudent));
        when(courseRepository.findByCourseCode("CS101")).thenReturn(Optional.of(mockCourse));

        Enrollments enrollment = enrollmentEntityMapper.mapToEntity(csvRecord);

        assertNotNull(enrollment);
        assertEquals(mockStudent, enrollment.getStudent());
        assertEquals(mockCourse, enrollment.getCourse());
    }

    /** Test Mapping When Student Does Not Exist **/
    @Test
    public void testMapToEntityShouldReturnEnrollmentWithNullStudentWhenStudentDoesNotExist() {
        when(csvRecord.get("student_id")).thenReturn("S999");
        when(csvRecord.get("course_id")).thenReturn("CS101");

        when(userRepository.findById("S999")).thenReturn(Optional.empty());
        when(courseRepository.findByCourseCode("CS101")).thenReturn(Optional.of(mockCourse));

        Enrollments enrollment = enrollmentEntityMapper.mapToEntity(csvRecord);

        assertNotNull(enrollment);
        assertNull(enrollment.getStudent());
        assertEquals(mockCourse, enrollment.getCourse());
    }

    /** Test Mapping When Course Does Not Exist **/
    @Test
    public void testMapToEntityShouldReturnEnrollmentWithNullCourseWhenCourseDoesNotExist() {
        when(csvRecord.get("student_id")).thenReturn("S123");
        when(csvRecord.get("course_id")).thenReturn("CS999");

        when(userRepository.findById("S123")).thenReturn(Optional.of(mockStudent));
        when(courseRepository.findByCourseCode("CS999")).thenReturn(Optional.empty());

        Enrollments enrollment = enrollmentEntityMapper.mapToEntity(csvRecord);

        assertNotNull(enrollment);
        assertEquals(mockStudent, enrollment.getStudent());
        assertNull(enrollment.getCourse());
    }

    /** Test Mapping When Both Student and Course Do Not Exist **/
    @Test
    public void testMapToEntityShouldReturnEnrollmentWithNullStudentAndNullCourseWhenBothDoNotExist() {
        when(csvRecord.get("student_id")).thenReturn("S999");
        when(csvRecord.get("course_id")).thenReturn("CS999");

        when(userRepository.findById("S999")).thenReturn(Optional.empty());
        when(courseRepository.findByCourseCode("CS999")).thenReturn(Optional.empty());

        Enrollments enrollment = enrollmentEntityMapper.mapToEntity(csvRecord);

        assertNotNull(enrollment);
        assertNull(enrollment.getStudent());
        assertNull(enrollment.getCourse());
    }
}