package services.mappers;

import models.Course;
import models.User;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import repository.core.UserRepository;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CourseEntityMapperTest {

    private CourseEntityMapper courseEntityMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CSVRecord csvRecord;

    @Mock
    private User mockProfessor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        courseEntityMapper = new CourseEntityMapper(userRepository);
    }

    /** Test Mapping When Professor Exists **/
    @Test
    public void testMapToEntityShouldReturnCourseWhenProfessorExists() {
        when(csvRecord.get("course_code")).thenReturn("CS101");
        when(csvRecord.get("course_name")).thenReturn("Computer Science");
        when(csvRecord.get("professor_id")).thenReturn(String.valueOf(123L));

        when(userRepository.findById(123L)).thenReturn(Optional.of(mockProfessor));

        Course course = courseEntityMapper.mapToEntity(csvRecord);

        assertNotNull(course);
        assertEquals("CS101", course.getCourseCode());
        assertEquals("Computer Science", course.getCourseName());
        assertEquals(mockProfessor, course.getProfessor());
    }

    /** Test Mapping When Professor Does Not Exist **/
    @Test
    public void testMapToEntityShouldReturnCourseWithNullProfessorWhenProfessorDoesNotExist() {
        when(csvRecord.get("course_code")).thenReturn("CS102");
        when(csvRecord.get("course_name")).thenReturn("Data Structures");
        when(csvRecord.get("professor_id")).thenReturn(String.valueOf(999L));

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Course course = courseEntityMapper.mapToEntity(csvRecord);

        assertNotNull(course);
        assertEquals("CS102", course.getCourseCode());
        assertEquals("Data Structures", course.getCourseName());
        assertNull(course.getProfessor());
    }
}