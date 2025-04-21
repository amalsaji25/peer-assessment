package services.processors;

import models.dto.Context;
import org.junit.Before;
import org.junit.Test;
import repository.core.Repository;
import services.mappers.EntityMapper;
import services.processors.record.InputRecord;
import services.validations.Validations;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class FormProcessorTest {

    private Validations<String> mockValidations;
    private EntityMapper<String> mockEntityMapper;
    private Repository<String> mockRepository;
    private InputRecord mockInputRecord;
    private Context mockContext;
    private FormProcessor<String> formProcessor;

    @Before
    public void setUp() {
        mockValidations = mock(Validations.class);
        mockEntityMapper = mock(EntityMapper.class);
        mockRepository = mock(Repository.class);
        mockInputRecord = mock(InputRecord.class);
        mockContext = new Context(); // You can use a mock here too if needed
        formProcessor = new FormProcessor<>(mockValidations, mockEntityMapper, mockRepository);
    }

    @Test
    public void testProcessData_success() throws Exception {
        when(mockValidations.validateSyntax(mockInputRecord)).thenReturn(true);
        List<String> entities = List.of("Entity1", "Entity2");

        when(mockEntityMapper.mapToEntityList(mockInputRecord, mockContext)).thenReturn(entities);
        when(mockValidations.validateSemantics(anyString(), eq(mockRepository))).thenReturn(true);

        CompletableFuture<List<String>> resultFuture = formProcessor.processData(mockInputRecord, mockContext);
        List<String> result = resultFuture.get();

        assertEquals(2, result.size());
        assertTrue(result.contains("Entity1"));
        verify(mockValidations, times(1)).validateSyntax(mockInputRecord);
    }

    @Test
    public void testProcessData_invalidSyntax_throwsException() {
        when(mockValidations.validateSyntax(mockInputRecord)).thenReturn(false);

        try {
            formProcessor.processData(mockInputRecord, mockContext).get();
            fail("Expected IllegalArgumentException");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
            assertEquals("Invalid form input: syntax validation failed", e.getCause().getMessage());
        }
    }

    @Test
    public void testProcessData_invalidSemantics_throwsException() {
        when(mockValidations.validateSyntax(mockInputRecord)).thenReturn(true);
        when(mockEntityMapper.mapToEntityList(mockInputRecord, mockContext)).thenReturn(List.of("bad"));
        when(mockValidations.validateSemantics(eq("bad"), eq(mockRepository))).thenReturn(false);

        try {
            formProcessor.processData(mockInputRecord, mockContext).get();
            fail("Expected IllegalStateException");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
            assertEquals("Not a valid record. Data might already exist.", e.getCause().getMessage());
        }
    }

    @Test
    public void testSaveProcessedData_allSuccess() throws Exception {
        List<String> processed = List.of("E1", "E2");

        Map<String, Object> status = new HashMap<>();
        status.put("successCount", 2);
        status.put("failedCount", 0);
        status.put("failedRecords", Collections.emptyList());

        when(mockRepository.saveAll(processed, mockContext)).thenReturn(CompletableFuture.completedFuture(status));

        CompletableFuture<String> resultFuture = formProcessor.saveProcessedData(processed, mockContext);
        String result = resultFuture.get();

        assertEquals("CSV uploaded and processed successfully.", result);
    }

    @Test
    public void testSaveProcessedData_partialFailure() throws Exception {
        List<String> processed = List.of("E1", "E2");

        Map<String, Object> status = new HashMap<>();
        status.put("successCount", 1);
        status.put("failedCount", 1);
        status.put("failedRecords", List.of("E2"));

        when(mockRepository.saveAll(processed, mockContext)).thenReturn(CompletableFuture.completedFuture(status));

        CompletableFuture<String> resultFuture = formProcessor.saveProcessedData(processed, mockContext);
        String result = resultFuture.get();

        assertTrue(result.contains("Partial success"));
    }

    @Test
    public void testSaveProcessedData_exceptionHandling() throws Exception {
        List<String> processed = List.of("E1");

        when(mockRepository.saveAll(processed, mockContext)).thenReturn(
                CompletableFuture.failedFuture(new RuntimeException("DB error")));

        CompletableFuture<String> resultFuture = formProcessor.saveProcessedData(processed, mockContext);
        String result = resultFuture.get();

        assertEquals("Failed to process CSV file.", result);
    }
}