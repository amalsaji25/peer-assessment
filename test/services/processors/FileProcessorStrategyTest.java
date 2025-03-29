package services.processors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.inject.Named;

import static org.junit.Assert.*;

public class FileProcessorStrategyTest {

    private FileProcessorStrategy fileProcessorStrategy;

    @Mock
    @Named("users")
    private FileProcessor userProcessor;

    @Mock
    @Named("courses")
    private FileProcessor courseProcessor;

    @Mock
    @Named("enrollments")
    private FileProcessor enrollmentProcessor;

    @Mock
    @Named("reviewTasks")
    private FileProcessor reviewTaskProcessor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        fileProcessorStrategy = new FileProcessorStrategy(userProcessor, courseProcessor, enrollmentProcessor, reviewTaskProcessor);
    }

    @Test
    public void testGetProcessorShouldReturnUserProcessorWhenFileTypeIsUsers() {
        assertEquals(userProcessor, fileProcessorStrategy.getProcessor("users"));
    }

    @Test
    public void testGetProcessorShouldReturnCourseProcessorWhenFileTypeIsCourses() {
        assertEquals(courseProcessor, fileProcessorStrategy.getProcessor("courses"));
    }

    @Test
    public void testGetProcessorShouldReturnEnrollmentProcessorWhenFileTypeIsEnrollments() {
        assertEquals(enrollmentProcessor, fileProcessorStrategy.getProcessor("enrollments"));
    }

    @Test
    public void testGetProcessorShouldReturnNullWhenFileTypeIsInvalid() {
        assertNull(fileProcessorStrategy.getProcessor("invalid"));
    }

    @Test
    public void testGetProcessorShouldBeCaseInsensitive() {
        assertEquals(userProcessor, fileProcessorStrategy.getProcessor("USERS"));
        assertEquals(courseProcessor, fileProcessorStrategy.getProcessor("CoUrSeS"));
        assertEquals(enrollmentProcessor, fileProcessorStrategy.getProcessor("EnRoLlMeNtS"));
    }
}