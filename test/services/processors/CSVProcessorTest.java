package services.processors;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import exceptions.InvalidCsvException;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import repository.core.Repository;
import services.mappers.EntityMapper;
import services.validations.Validations;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@RunWith(MockitoJUnitRunner.class)
public class CSVProcessorTest {

    @Mock
    private Validations<Object> validations;

    @Mock
    private EntityMapper<Object> entityMapper;

    @Mock
    private Repository<Object> repository;

    @InjectMocks
    private CSVProcessor<Object> csvProcessor;

    private Path mockFilePath;

    @Before
    public void setUp() throws IOException {
        mockFilePath = Paths.get("mock-file.csv");

        String csvContent = "col1,col2,col3\nval1,val2,val3\n";
        Files.write(mockFilePath, csvContent.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        when(validations.validateFieldOrder(anyList())).thenReturn(true);
        when(validations.validateSyntax(any())).thenReturn(true);
        when(validations.validateSemantics(any(), any())).thenReturn(true);
        when(entityMapper.mapToEntityList(any())).thenReturn(List.of(new Object()));
    }

    @After
    public void tearDown() throws IOException {
        Files.deleteIfExists(mockFilePath);
    }

    @Test
    public void testParseAndProcessFile_ShouldReturnValidList() {
        List<Object> result = csvProcessor.parseAndProcessFile(mockFilePath).join();
        assertFalse(result.isEmpty());
    }

    @Test
    public void testParseAndProcessFile_ShouldFailIfHeaderInvalid() {
        when(validations.validateFieldOrder(anyList())).thenReturn(false);

        CompletionException exception = assertThrows(
                CompletionException.class,
                () -> csvProcessor.parseAndProcessFile(mockFilePath).join()
        );

        assertTrue(exception.getCause() instanceof InvalidCsvException);
        assertEquals("CSV file has incorrect field order.", exception.getCause().getMessage());
    }

    @Test
    public void testParseAndProcessFile_ShouldFailIfNoValidSemanticRecords() {
        when(validations.validateSemantics(any(), any())).thenReturn(false);
        CompletionException exception = assertThrows(
                CompletionException.class,
                () -> csvProcessor.parseAndProcessFile(mockFilePath).join()
        );

        assertTrue(exception.getCause() instanceof InvalidCsvException);
        assertEquals("No valid records found after semantic validation.", exception.getCause().getMessage());
    }

    @Test
    public void testSaveProcessedFileData_ShouldReturnSuccessMessage() {
        List<Object> data = List.of(new Object());
        when(repository.saveAll(data)).thenReturn(CompletableFuture.completedFuture(Map.of(
                "successCount", 1,
                "failedCount", 0,
                "failedRecords", List.of()
        )));

        String message = csvProcessor.saveProcessedFileData(data).join();
        assertEquals("CSV uploaded and processed successfully.", message);
    }

    @Test
    public void testSaveProcessedFileData_ShouldReturnPartialSuccessMessage() {
        List<Object> data = List.of(new Object());
        when(repository.saveAll(data)).thenReturn(CompletableFuture.completedFuture(Map.of(
                "successCount", 1,
                "failedCount", 1,
                "failedRecords", List.of("record1")
        )));

        String message = csvProcessor.saveProcessedFileData(data).join();
        assertEquals("Partial success: 1 records saved, 1 failed.", message);
    }

    @Test
    public void testSaveProcessedFileData_ShouldReturnErrorMessageOnException() {
        List<Object> data = List.of(new Object());

        when(repository.saveAll(data))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("DB failure")));

        String message = csvProcessor.saveProcessedFileData(data).join();

        assertEquals("Failed to process CSV file.", message);
    }
}