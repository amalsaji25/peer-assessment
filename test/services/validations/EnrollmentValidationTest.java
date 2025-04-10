package services.validations;

import models.Course;
import models.Enrollment;
import models.User;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import repository.core.Repository;
import services.processors.record.InputRecord;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class EnrollmentValidationTest {

    private EnrollmentValidation enrollmentValidation;

    @Mock
    private Repository<Enrollment> repository;

    @Mock
    private InputRecord csvRecord;

    @Mock
    private Enrollment mockEnrollment;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        enrollmentValidation = new EnrollmentValidation();
    }

    /** Test Syntax Validation **/
    @Test
    public void testValidateSyntaxShouldReturnTrueWhenAllMandatoryFieldsArePresent() {
        when(csvRecord.isMapped(anyString())).thenReturn(true);
        when(csvRecord.get(anyString())).thenReturn("ValidData");

        assertTrue(enrollmentValidation.validateSyntax(csvRecord));
    }

    @Test
    public void testValidateSyntaxShouldReturnFalseWhenMandatoryFieldsAreMissing() {
        when(csvRecord.isMapped("student_id")).thenReturn(false);

        assertFalse(enrollmentValidation.validateSyntax(csvRecord));
    }

    /** Test Semantic Validation **/
    @Test
    public void testValidateSemanticsShouldReturnTrueWhenStudentAndCourseExist() {
        Course mockCourse = Mockito.mock(Course.class);
        User mockUser = Mockito.mock(User.class);
        when(mockEnrollment.getStudent()).thenReturn(mockUser);
        when(mockEnrollment.getCourse()).thenReturn(mockCourse);

        assertTrue(enrollmentValidation.validateSemantics(mockEnrollment, repository));
    }

    @Test
    public void testValidateSemanticsShouldReturnFalseWhenStudentIsNull() {
        Course mockCourse = Mockito.mock(Course.class);
        when(mockEnrollment.getStudent()).thenReturn(null);
        when(mockEnrollment.getCourse()).thenReturn(mockCourse);

        assertFalse(enrollmentValidation.validateSemantics(mockEnrollment, repository));
    }

    @Test
    public void testValidateSemanticsShouldReturnFalseWhenCourseIsNull() {
        User mockUser = Mockito.mock(User.class);
        when(mockEnrollment.getStudent()).thenReturn(mockUser);
        when(mockEnrollment.getCourse()).thenReturn(null);

        assertFalse(enrollmentValidation.validateSemantics(mockEnrollment, repository));
    }

    /** Test Field Order Validation **/
    @Test
    public void testValidateFieldOrderShouldReturnTrueWhenOrderIsCorrect() {
        List<String> correctHeaders = Arrays.asList("student_id", "course_code","course_section","term");

        assertTrue(enrollmentValidation.validateFieldOrder(correctHeaders));
    }

    @Test
    public void testValidateFieldOrderShouldReturnFalseWhenOrderIsIncorrect() {
        List<String> incorrectHeaders = Arrays.asList("course_code","course_section", "student_id");

        assertFalse(enrollmentValidation.validateFieldOrder(incorrectHeaders));
    }
}