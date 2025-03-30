package services;

import exceptions.InvalidFileUploadException;
import models.dto.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import services.processors.Processor;
import services.processors.ProcessorStrategy;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FileUploadServiceTest {

    @Mock
    private ProcessorStrategy processorStrategy;

    @Mock
    private Processor<Object,Path> processor;

    @InjectMocks
    private FileUploadService fileUploadService;

    private File mockFile;
    private Path mockPath;
    private Context mockContext;

    @Before
    public void setup() {
        mockFile = mock(File.class);
        mockPath = mock(Path.class);

        when(mockFile.getName()).thenReturn("test.csv");
        when(mockFile.toPath()).thenReturn(mockPath);
    }

    @Test
    public void testGetFileProcessorShouldThrowForInvalidFileExtension() {
        when(mockFile.getName()).thenReturn("invalid.txt");

        CompletionException ex = assertThrows(CompletionException.class, () ->
                fileUploadService.getFileProcessor(mockFile, "users").join()
        );

        assertTrue(ex.getCause() instanceof InvalidFileUploadException);
        assertEquals("Invalid file format. Only CSV files are allowed.", ex.getCause().getMessage());
    }

    @Test
    public void testGetFileProcessorShouldThrowForInvalidFileType() {
        when(processorStrategy.getFileProcessor("invalid")).thenReturn(null);
        when(mockFile.getName()).thenReturn("valid.csv");

        CompletionException ex = assertThrows(CompletionException.class, () ->
                fileUploadService.getFileProcessor(mockFile, "invalid").join()
        );

        assertTrue(ex.getCause() instanceof InvalidFileUploadException);
        assertEquals("Invalid fileType. Allowed: users, courses, enrollments.", ex.getCause().getMessage());
    }

    @Test
    public void testGetFileProcessorShouldReturnValidProcessor() {
        when(processorStrategy.getFileProcessor("users")).thenReturn(processor);

        Processor<Object, Path> result = fileUploadService.getFileProcessor(mockFile, "users").join();
        assertNotNull(result);
    }

    @Test
    public void testParseAndSaveSuccessfully() {
        when(processorStrategy.getFileProcessor("users")).thenReturn(processor);

        List<Object> parsedData = List.of(new Object());

        when(processor.processData(mockPath, mockContext))
                .thenReturn(CompletableFuture.completedFuture(parsedData));

        when(processor.saveProcessedData(parsedData, mockContext))
                .thenReturn(CompletableFuture.completedFuture("Success: All records saved."));

        CompletableFuture<Processor<Object, Path>> processorFuture = fileUploadService.getFileProcessor(mockFile, "users");

        CompletableFuture<String> resultFuture = processorFuture
                .thenCompose(processor -> fileUploadService.parseAndProcessFile(processor, mockFile, mockContext)
                        .thenCompose(data -> fileUploadService.saveProcessedFileData(processor, data, mockContext)));

        String result = resultFuture.join();
        assertEquals("Success: All records saved.", result);
    }
}