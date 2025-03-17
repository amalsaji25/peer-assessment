package services.validations;

import models.Courses;
import models.Enrollments;
import models.Users;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import repository.Repository;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class EnrollmentValidationTest {

    private EnrollmentValidation enrollmentValidation;

    @Mock
    private Repository<Enrollments> repository;

    @Mock
    private CSVRecord csvRecord;

    @Mock
    private Enrollments mockEnrollment;

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
        Courses mockCourse = Mockito.mock(Courses.class);
        Users mockUser = Mockito.mock(Users.class);
        when(mockEnrollment.getStudent()).thenReturn(mockUser);
        when(mockEnrollment.getCourse()).thenReturn(mockCourse);

        assertTrue(enrollmentValidation.validateSemantics(mockEnrollment, repository));
    }

    @Test
    public void testValidateSemanticsShouldReturnFalseWhenStudentIsNull() {
        Courses mockCourse = Mockito.mock(Courses.class);
        when(mockEnrollment.getStudent()).thenReturn(null);
        when(mockEnrollment.getCourse()).thenReturn(mockCourse);

        assertFalse(enrollmentValidation.validateSemantics(mockEnrollment, repository));
    }

    @Test
    public void testValidateSemanticsShouldReturnFalseWhenCourseIsNull() {
        Users mockUser = Mockito.mock(Users.class);
        when(mockEnrollment.getStudent()).thenReturn(mockUser);
        when(mockEnrollment.getCourse()).thenReturn(null);

        assertFalse(enrollmentValidation.validateSemantics(mockEnrollment, repository));
    }

    /** Test Field Order Validation **/
    @Test
    public void testValidateFieldOrderShouldReturnTrueWhenOrderIsCorrect() {
        List<String> correctHeaders = Arrays.asList("student_id", "course_id");

        assertTrue(enrollmentValidation.validateFieldOrder(correctHeaders));
    }

    @Test
    public void testValidateFieldOrderShouldReturnFalseWhenOrderIsIncorrect() {
        List<String> incorrectHeaders = Arrays.asList("course_id", "student_id");

        assertFalse(enrollmentValidation.validateFieldOrder(incorrectHeaders));
    }
}