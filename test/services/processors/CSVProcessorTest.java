package services.processors;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import exceptions.InvalidCsvException;
import models.dto.Context;
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

    private Context mockContext;

    @Before
    public void setUp() throws IOException {
        mockFilePath = Paths.get("mock-file.csv");

        String csvContent = "col1,col2,col3\nval1,val2,val3\n";
        Files.write(mockFilePath, csvContent.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        when(validations.validateFieldOrder(anyList())).thenReturn(true);
        when(validations.validateSyntax(any())).thenReturn(true);
        when(validations.validateSemantics(any(), any())).thenReturn(true);
        when(entityMapper.mapToEntityList(any(), eq(mockContext))).thenReturn(List.of(new Object()));
    }

    @After
    public void tearDown() throws IOException {
        Files.deleteIfExists(mockFilePath);
    }

    @Test
    public void testParseAndProcessFile_ShouldReturnValidList() {
        List<Object> result = csvProcessor.processData(mockFilePath, mockContext).join();
        assertFalse(result.isEmpty());
    }

    @Test
    public void testParseAndProcessFile_ShouldFailIfHeaderInvalid() {
        when(validations.validateFieldOrder(anyList())).thenReturn(false);

        CompletionException exception = assertThrows(
                CompletionException.class,
                () -> csvProcessor.processData(mockFilePath, mockContext).join()
        );

        assertTrue(exception.getCause() instanceof InvalidCsvException);
        assertEquals("CSV file has incorrect field order.", exception.getCause().getMessage());
    }

    @Test
    public void testParseAndProcessFile_ShouldFailIfNoValidSemanticRecords() {
        when(validations.validateSemantics(any(), any())).thenReturn(false);
        CompletionException exception = assertThrows(
                CompletionException.class,
                () -> csvProcessor.processData(mockFilePath, mockContext).join()
        );

        assertTrue(exception.getCause() instanceof InvalidCsvException);
        assertEquals("No valid records found. Data might already exist.", exception.getCause().getMessage());
    }

    @Test
    public void testSaveProcessedFileData_ShouldReturnSuccessMessage() {
        List<Object> data = List.of(new Object());
        when(repository.saveAll(data, mockContext)).thenReturn(CompletableFuture.completedFuture(Map.of(
                "successCount", 1,
                "failedCount", 0,
                "failedRecords", List.of()
        )));

        String message = csvProcessor.saveProcessedData(data, mockContext).join();
        assertEquals("Upload completed successfully", message);
    }

    @Test
    public void testSaveProcessedFileData_ShouldReturnPartialSuccessMessage() {
        List<Object> data = List.of(new Object());
        when(repository.saveAll(data, mockContext)).thenReturn(CompletableFuture.completedFuture(Map.of(
                "successCount", 1,
                "failedCount", 1,
                "failedRecords", List.of("record1")
        )));

        String message = csvProcessor.saveProcessedData(data, mockContext).join();
        assertEquals("Upload completed: 1 new records added, 1 duplicate records skipped.", message);
    }

    @Test
    public void testSaveProcessedFileData_ShouldReturnErrorMessageOnException() {
        List<Object> data = List.of(new Object());

        when(repository.saveAll(data, mockContext))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("DB failure")));

        String message = csvProcessor.saveProcessedData(data, mockContext).join();

        assertEquals("An error occurred while saving the records.", message);
    }
}