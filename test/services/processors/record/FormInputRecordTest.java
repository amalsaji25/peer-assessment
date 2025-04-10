package services.processors.record;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class FormInputRecordTest {

    private FormInputRecord record;

    @Before
    public void setUp() {
        Map<String, String[]> formParams = new HashMap<>();
        formParams.put("name", new String[] {"John"});
        formParams.put("email", new String[] {"john@example.com"});
        formParams.put("empty", new String[] {});  // empty array
        formParams.put("nullArray", null);         // null array

        record = new FormInputRecord(formParams);
    }

    @Test
    public void testGet_returnsValueIfMapped() {
        assertEquals("John", record.get("name"));
        assertEquals("john@example.com", record.get("email"));
    }

    @Test
    public void testGet_returnsEmptyStringIfNotMapped() {
        assertEquals("", record.get("nonexistent"));
    }

    @Test
    public void testIsMapped_returnsTrueIfMapped() {
        assertTrue(record.isMapped("name"));
        assertTrue(record.isMapped("email"));
    }

    @Test
    public void testIsMapped_returnsFalseIfNotMapped() {
        assertFalse(record.isMapped("nonexistent"));
        assertFalse(record.isMapped("empty"));       // empty array shouldn't be added
        assertFalse(record.isMapped("nullArray"));   // null value shouldn't be added
    }

    @Test
    public void testToString_containsKeys() {
        String output = record.toString();
        assertTrue(output.contains("name"));
        assertTrue(output.contains("email"));
        assertFalse(output.contains("nonexistent"));
    }
}