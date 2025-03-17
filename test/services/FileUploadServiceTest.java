package services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import play.mvc.Result;
import services.processors.FileProcessor;
import services.processors.FileProcessorStrategy;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.OK;

@RunWith(MockitoJUnitRunner.class)
public class FileUploadServiceTest {

    @Mock
    private FileProcessorStrategy fileProcessorStrategy;

    @Mock
    private FileProcessor fileProcessor;

    @InjectMocks
    private FileUploadService fileUploadService;

    private File mockFile;

    @Before
    public void setup() {
        mockFile = mock(File.class);
        when(mockFile.getName()).thenReturn("test.csv");
    }

    @Test
    public void testProcessFileUploadShouldReturnBadRequestWhenFileIsInvalid() {
        when(mockFile.getName()).thenReturn("test.txt"); // Invalid file type

        CompletionStage<Result> result = fileUploadService.processFileUpload(mockFile, "users");
        assertEquals(BAD_REQUEST, result.toCompletableFuture().join().status());
    }

    @Test
    public void testProcessFileUploadShouldReturnBadRequestWhenFileTypeInvalid() {
        when(fileProcessorStrategy.getProcessor("invalidType")).thenReturn(null);

        CompletionStage<Result> result = fileUploadService.processFileUpload(mockFile, "invalidType");
        assertEquals(BAD_REQUEST, result.toCompletableFuture().join().status());
    }

    @Test
    public void testProcessFileUploadShouldProcessFileSuccessfully() {
        when(mockFile.toPath()).thenReturn(mock(Path.class));
        when(fileProcessorStrategy.getProcessor("users")).thenReturn(fileProcessor);
        when(fileProcessor.processFile(any(Path.class), eq("users")))
                .thenReturn(CompletableFuture.completedFuture(play.mvc.Results.ok()));

        CompletionStage<Result> result = fileUploadService.processFileUpload(mockFile, "users");

        assertEquals(OK, result.toCompletableFuture().join().status());
    }
}
