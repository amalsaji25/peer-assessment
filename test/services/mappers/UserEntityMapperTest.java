package services.mappers;

import models.Users;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class UserEntityMapperTest {

    private UserEntityMapper userEntityMapper;

    @Mock
    private CSVRecord csvRecord;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        userEntityMapper = new UserEntityMapper();
    }

    /** Test Valid User Mapping **/
    @Test
    public void testMapToEntityShouldReturnUserWhenAllFieldsArePresent() {
        when(csvRecord.get("user_id")).thenReturn(String.valueOf(123L));
        when(csvRecord.get("email")).thenReturn("user@example.com");
        when(csvRecord.get("password")).thenReturn("password123");
        when(csvRecord.get("first_name")).thenReturn("John");
        when(csvRecord.isMapped("middle_name")).thenReturn(true);
        when(csvRecord.get("middle_name")).thenReturn("M");
        when(csvRecord.get("last_name")).thenReturn("Doe");
        when(csvRecord.get("role")).thenReturn("student");

        Users user = userEntityMapper.mapToEntity(csvRecord);

        assertNotNull(user);
        assertEquals(Long.valueOf(123L), user.getUserId());
        assertEquals("user@example.com", user.getEmail());
        assertEquals("John", user.getFirstName());
        assertEquals("M", user.getMiddleName());
        assertEquals("Doe", user.getLastName());
        assertEquals("student", user.getRole());
    }

    /** Test Missing Middle Name **/
    @Test
    public void testMapToEntityShouldAssignEmptyStringWhenMiddleNameIsMissing() {
        when(csvRecord.get("user_id")).thenReturn(String.valueOf(123L));
        when(csvRecord.get("email")).thenReturn("user@example.com");
        when(csvRecord.get("password")).thenReturn("password123");
        when(csvRecord.get("first_name")).thenReturn("John");
        when(csvRecord.isMapped("middle_name")).thenReturn(false);
        when(csvRecord.get("last_name")).thenReturn("Doe");
        when(csvRecord.get("role")).thenReturn("professor");

        Users user = userEntityMapper.mapToEntity(csvRecord);

        assertNotNull(user);
        assertEquals(Long.valueOf(123L), user.getUserId());
        assertEquals("John", user.getFirstName());
        assertEquals("", user.getMiddleName());  // Middle name should be an empty string
        assertEquals("Doe", user.getLastName());
        assertEquals("professor", user.getRole());
    }

    /** Test Handling of Empty Fields **/
    @Test
    public void testMapToEntityShouldHandleEmptyFields() {
        when(csvRecord.get("user_id")).thenReturn(String.valueOf(0L));
        when(csvRecord.get("email")).thenReturn("");
        when(csvRecord.get("password")).thenReturn("");
        when(csvRecord.get("first_name")).thenReturn("");
        when(csvRecord.isMapped("middle_name")).thenReturn(false);
        when(csvRecord.get("last_name")).thenReturn("");
        when(csvRecord.get("role")).thenReturn("");

        Users user = userEntityMapper.mapToEntity(csvRecord);

        assertNotNull(user);
        assertEquals(Long.valueOf(0L), user.getUserId());
        assertEquals("", user.getEmail());
        assertEquals("", user.getFirstName());
        assertEquals("", user.getMiddleName());  // Empty due to missing field
        assertEquals("", user.getLastName());
        assertEquals("", user.getRole());
    }
}