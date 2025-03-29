package services;

import exceptions.InvalidFileUploadException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import services.processors.FileProcessor;
import services.processors.FileProcessorStrategy;

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
    private FileProcessorStrategy fileProcessorStrategy;

    @Mock
    private FileProcessor<Object> fileProcessor;

    @InjectMocks
    private FileUploadService fileUploadService;

    private File mockFile;
    private Path mockPath;

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
        when(fileProcessorStrategy.getProcessor("invalid")).thenReturn(null);
        when(mockFile.getName()).thenReturn("valid.csv");

        CompletionException ex = assertThrows(CompletionException.class, () ->
                fileUploadService.getFileProcessor(mockFile, "invalid").join()
        );

        assertTrue(ex.getCause() instanceof InvalidFileUploadException);
        assertEquals("Invalid fileType. Allowed: users, courses, enrollments.", ex.getCause().getMessage());
    }

    @Test
    public void testGetFileProcessorShouldReturnValidProcessor() {
        when(fileProcessorStrategy.getProcessor("users")).thenReturn(fileProcessor);

        FileProcessor<Object> result = fileUploadService.getFileProcessor(mockFile, "users").join();
        assertNotNull(result);
    }

    @Test
    public void testParseAndSaveSuccessfully() {
        when(fileProcessorStrategy.getProcessor("users")).thenReturn(fileProcessor);

        List<Object> parsedData = List.of(new Object());

        when(fileProcessor.parseAndProcessFile(mockPath))
                .thenReturn(CompletableFuture.completedFuture(parsedData));

        when(fileProcessor.saveProcessedFileData(parsedData))
                .thenReturn(CompletableFuture.completedFuture("Success: All records saved."));

        CompletableFuture<FileProcessor<Object>> processorFuture = fileUploadService.getFileProcessor(mockFile, "users");

        CompletableFuture<String> resultFuture = processorFuture
                .thenCompose(processor -> fileUploadService.parseAndProcessFile(processor, mockFile)
                        .thenCompose(data -> fileUploadService.saveProcessedFileData(processor, data)));

        String result = resultFuture.join();
        assertEquals("Success: All records saved.", result);
    }
}