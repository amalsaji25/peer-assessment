package services.processors.record;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CSVInputRecordTest {

    private CSVRecord mockCSVRecord;
    private CSVInputRecord csvInputRecord;

    @Before
    public void setUp() {
        mockCSVRecord = mock(CSVRecord.class);
        when(mockCSVRecord.get("name")).thenReturn("Alice");
        when(mockCSVRecord.isMapped("name")).thenReturn(true);
        when(mockCSVRecord.isMapped("email")).thenReturn(false);

        csvInputRecord = new CSVInputRecord(mockCSVRecord);
    }

    @Test
    public void testGet_returnsCorrectValue() {
        assertEquals("Alice", csvInputRecord.get("name"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGet_throwsExceptionForUnmappedKey() {
        // Simulates real behavior of CSVRecord when key isn't mapped
        when(mockCSVRecord.get("email")).thenThrow(new IllegalArgumentException("Column not found"));
        csvInputRecord.get("email");
    }

    @Test
    public void testIsMapped_returnsTrueIfMapped() {
        assertTrue(csvInputRecord.isMapped("name"));
    }

    @Test
    public void testIsMapped_returnsFalseIfNotMapped() {
        assertFalse(csvInputRecord.isMapped("email"));
    }
}