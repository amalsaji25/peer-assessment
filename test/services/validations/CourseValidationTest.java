package services.validations;

import models.Course;
import models.User;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import repository.core.CourseRepository;
import services.processors.record.InputRecord;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CourseValidationTest {

    private CourseValidation courseValidation;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private InputRecord csvRecord;

    @Mock
    private Course mockCourse;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        courseValidation = new CourseValidation();
    }

    // ---------------------- Syntax Validation Tests ----------------------

    @Test
    public void testValidateSyntaxShouldReturnTrueWhenAllMandatoryFieldsArePresent() {
        when(csvRecord.isMapped("course_code")).thenReturn(true);
        when(csvRecord.get("course_code")).thenReturn("CS101");

        when(csvRecord.isMapped("course_name")).thenReturn(true);
        when(csvRecord.get("course_name")).thenReturn("Computer Science");

        when(csvRecord.isMapped("course_section")).thenReturn(true);
        when(csvRecord.get("course_section")).thenReturn("SS");

        when(csvRecord.isMapped("professor_id")).thenReturn(true);
        when(csvRecord.get("professor_id")).thenReturn("P123");

        when(csvRecord.isMapped("term")).thenReturn(true);
        when(csvRecord.get("term")).thenReturn("Fall 2023");

        assertTrue(courseValidation.validateSyntax(csvRecord));
    }

    @Test
    public void testValidateSyntaxShouldReturnFalseWhenMandatoryFieldIsMissing() {
        when(csvRecord.isMapped("course_code")).thenReturn(true);
        when(csvRecord.get("course_code")).thenReturn("CS101");

        when(csvRecord.isMapped("course_name")).thenReturn(false);
        when(csvRecord.isMapped("course_section")).thenReturn(true);
        when(csvRecord.isMapped("professor_id")).thenReturn(true);
        when(csvRecord.get("professor_id")).thenReturn("P123");
        when(csvRecord.isMapped("term")).thenReturn(true);
        when(csvRecord.get("term")).thenReturn("Fall 2023");

        assertFalse(courseValidation.validateSyntax(csvRecord));
    }

    @Test
    public void testValidateSyntaxShouldReturnFalseWhenMandatoryFieldIsEmpty() {
        when(csvRecord.isMapped("course_code")).thenReturn(true);
        when(csvRecord.get("course_code")).thenReturn("CS101");

        when(csvRecord.isMapped("course_name")).thenReturn(true);
        when(csvRecord.get("course_name")).thenReturn("");

        when(csvRecord.isMapped("course_section")).thenReturn(true);
        when(csvRecord.get("course_section")).thenReturn("SS");

        when(csvRecord.isMapped("professor_id")).thenReturn(true);
        when(csvRecord.get("professor_id")).thenReturn("P123");

        when(csvRecord.isMapped("term")).thenReturn(true);
        when(csvRecord.get("term")).thenReturn("Fall 2023");

        assertFalse(courseValidation.validateSyntax(csvRecord));
    }

    // ---------------------- Semantic Validation Tests ----------------------

    @Test
    public void testValidateSemanticsShouldReturnFalseWhenCourseAlreadyExists() {
        when(mockCourse.getCourseCode()).thenReturn("CS101");
        when(courseRepository.findByCourseCodeAndSectionAndTerm("CS101", "SS", "Fall 2024")).thenReturn(Optional.of(mockCourse));

        assertFalse(courseValidation.validateSemantics(mockCourse, courseRepository));
    }

    @Test
    public void testValidateSemanticsShouldReturnFalseWhenProfessorIsNull() {
        when(mockCourse.getCourseCode()).thenReturn("CS102");
        when(mockCourse.getProfessor()).thenReturn(null); // Professor is missing
        when(courseRepository.findByCourseCodeAndSectionAndTerm("CS102", "SS", "Fall 2024")).thenReturn(Optional.empty());

        assertFalse(courseValidation.validateSemantics(mockCourse, courseRepository));
    }

    @Test
    public void testValidateSemanticsShouldReturnTrueWhenValidCourseAndProfessorExists() {
        User mockProfessor = mock(User.class);
        when(mockCourse.getCourseCode()).thenReturn("CS103");
        when(mockCourse.getProfessor()).thenReturn(mockProfessor);
        when(courseRepository.findByCourseCodeAndSectionAndTerm("CS103", "SS", "Fall 2024")).thenReturn(Optional.empty());

        assertTrue(courseValidation.validateSemantics(mockCourse, courseRepository));
    }

    // ---------------------- Field Order Validation Tests ----------------------

    @Test
    public void testValidateFieldOrderShouldReturnTrueWhenFieldsAreInCorrectOrder() {
        List<String> correctHeaders = List.of("course_code", "course_name", "course_section", "professor_id", "term");
        assertTrue(courseValidation.validateFieldOrder(correctHeaders));
    }

    @Test
    public void testValidateFieldOrderShouldReturnFalseWhenFieldsAreInIncorrectOrder() {
        List<String> incorrectHeaders = List.of("course_name", "course_code", "professor_id", "term");
        boolean result = courseValidation.validateFieldOrder(incorrectHeaders);

        assertFalse("Field order validation should return false for incorrect order", result);
    }
}