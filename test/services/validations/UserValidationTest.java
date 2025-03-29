package services.validations;

import models.User;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import repository.core.UserRepository;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class UserValidationTest {

    private UserValidation userValidation;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CSVRecord csvRecord;

    @Mock
    private User mockUser;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        userValidation = new UserValidation();
    }

    /** Test Syntax Validation **/
    @Test
    public void testValidateSyntaxShouldReturnTrueWhenAllMandatoryFieldsArePresent() {
        when(csvRecord.isMapped(anyString())).thenReturn(true);
        when(csvRecord.get(anyString())).thenReturn("ValidData");

        assertTrue(userValidation.validateSyntax(csvRecord));
    }

    @Test
    public void testValidateSyntaxShouldReturnFalseWhenMandatoryFieldsAreMissing() {
        when(csvRecord.isMapped("email")).thenReturn(false);

        assertFalse(userValidation.validateSyntax(csvRecord));
    }

    /** Test Semantic Validation **/
    @Test
    public void testValidateSemanticsShouldReturnTrueWhenUserIsValid() {
        when(mockUser.getUserId()).thenReturn(123L);
        when(mockUser.getEmail()).thenReturn("valid@example.com");
        when(mockUser.getRole()).thenReturn("student");
        when(userRepository.findById(123L)).thenReturn(Optional.empty());

        assertTrue(userValidation.validateSemantics(mockUser, userRepository));
    }

    @Test
    public void testValidateSemanticsShouldReturnFalseWhenUserAlreadyExists() {
        when(mockUser.getUserId()).thenReturn(123L);
        when(userRepository.findById(123L)).thenReturn(Optional.of(mockUser));

        assertFalse(userValidation.validateSemantics(mockUser, userRepository));
    }

    @Test
    public void testValidateSemanticsShouldReturnFalseWhenEmailIsInvalid() {
        when(mockUser.getUserId()).thenReturn(123L);
        when(mockUser.getEmail()).thenReturn("invalid-email");
        when(mockUser.getRole()).thenReturn("student");
        when(userRepository.findById(123L)).thenReturn(Optional.empty());

        assertFalse(userValidation.validateSemantics(mockUser, userRepository));
    }

    @Test
    public void testValidateSemanticsShouldReturnFalseWhenRoleIsInvalid() {
        when(mockUser.getUserId()).thenReturn(123L);
        when(mockUser.getEmail()).thenReturn("valid@example.com");
        when(mockUser.getRole()).thenReturn("admin"); // Invalid Role
        when(userRepository.findById(123L)).thenReturn(Optional.empty());

        assertFalse(userValidation.validateSemantics(mockUser, userRepository));
    }

    /** Test Field Order Validation **/
    @Test
    public void testValidateFieldOrderShouldReturnTrueWhenOrderIsCorrect() {
        List<String> correctHeaders = Arrays.asList("user_id", "email", "password", "first_name", "last_name", "role");

        assertTrue(userValidation.validateFieldOrder(correctHeaders));
    }

    @Test
    public void testValidateFieldOrderShouldReturnFalseWhenOrderIsIncorrect() {
        List<String> incorrectHeaders = Arrays.asList("email", "user_id", "password", "first_name", "last_name", "role");

        assertFalse(userValidation.validateFieldOrder(incorrectHeaders));
    }
}