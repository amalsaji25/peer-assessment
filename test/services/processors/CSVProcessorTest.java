package services.processors;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import static play.test.Helpers.*;

import org.junit.Test;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import play.mvc.Result;
import repository.Repository;
import services.mappers.EntityMapper;
import services.validations.Validations;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

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

        String csvContent = "header1,header2,header3\nvalue1,value2,value3\n";
        Files.write(mockFilePath, csvContent.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        when(validations.validateFieldOrder(anyList())).thenReturn(true);
        when(validations.validateSyntax(any())).thenReturn(true);
        when(validations.validateSemantics(any(), any())).thenReturn(true);

        when(entityMapper.mapToEntity(any())).thenReturn(new Object());

        when(repository.saveAll(anyList())).thenReturn(CompletableFuture.completedFuture(Map.of(
                "successCount", 1,
                "failedCount", 0,
                "failedRecords", Collections.emptyList()
        )));
    }

    @After
    public void tearDown() throws IOException {
        Files.deleteIfExists(mockFilePath);
    }

    @Test
    public void testProcessFileShouldReturnSuccessWhenProcessingValidRecords() {
        CompletionStage<Result> resultStage = csvProcessor.processFile(mockFilePath, "csv");
        Result result = resultStage.toCompletableFuture().join();

        String body = contentAsString(result);

        assertEquals(200, result.status());
        assertEquals("CSV uploaded and processed successfully.", body);
    }

    @Test
    public void testProcessFileShouldReturnBadRequestWhenFieldOrderInvalid() {
        when(validations.validateFieldOrder(anyList())).thenReturn(false);

        CompletionStage<Result> resultStage = csvProcessor.processFile(mockFilePath, "csv");
        Result result = resultStage.toCompletableFuture().join();

        String body = contentAsString(result);

        assertEquals(400, result.status());
        assertEquals("CSV file has incorrect field order.", body);
    }

    @Test
    public void testProcessFileShouldReturnBadRequestWhenNoValidRecordsFound() {
        when(validations.validateSemantics(any(), any())).thenReturn(false);

        CompletionStage<Result> resultStage = csvProcessor.processFile(mockFilePath, "csv");
        Result result = resultStage.toCompletableFuture().join();

        String body = contentAsString(result);

        assertEquals(400, result.status());
        assertEquals("No valid records found. Check logs for details.", body);
    }

    @Test
    public void testProcessFileShouldReturnPartialSuccessWhenSomeRecordsFail() {
        when(repository.saveAll(anyList())).thenReturn(CompletableFuture.completedFuture(Map.of(
                "successCount", 1,
                "failedCount", 1,
                "failedRecords", List.of("failedRecord1")
        )));

        CompletionStage<Result> resultStage = csvProcessor.processFile(mockFilePath, "csv");
        Result result = resultStage.toCompletableFuture().join();

        String body = contentAsString(result);

        assertEquals(200, result.status());
        assertEquals("Partial success: 1 records saved, 1 failed.", body);
    }

    @Test
    public void testProcessFileShouldReturnInternalServerErrorOnException() {
        when(repository.saveAll(anyList())).thenThrow(new RuntimeException("Database Error"));

        CompletionStage<Result> resultStage = csvProcessor.processFile(mockFilePath, "csv");
        Result result = resultStage.toCompletableFuture().join();

        String body = contentAsString(result);

        assertEquals(500, result.status());
        assertEquals("Error processing CSV file.", body);
    }
}