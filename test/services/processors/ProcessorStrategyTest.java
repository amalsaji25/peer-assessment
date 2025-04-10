package services.processors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.inject.Named;

import static org.junit.Assert.*;

public class ProcessorStrategyTest {

    private ProcessorStrategy processorStrategy;

    @Mock
    @Named("users")
    private Processor userProcessor;

    @Mock
    @Named("courses")
    private Processor courseProcessor;

    @Mock
    @Named("enrollments")
    private Processor enrollmentProcessor;

    @Mock
    @Named("reviewTasks")
    private Processor reviewTaskProcessor;

    @Mock
    @Named("userForm")
    private Processor userFormProcessor;

    @Mock
    @Named("courseForm")
    private Processor courseFormProcessor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        processorStrategy = new ProcessorStrategy(userProcessor, courseProcessor, enrollmentProcessor, reviewTaskProcessor, userFormProcessor, courseFormProcessor);
    }

    @Test
    public void testGetProcessorShouldReturnUserProcessorWhenFileTypeIsUsers() {
        assertEquals(userProcessor, processorStrategy.getFileProcessor("users"));
    }

    @Test
    public void testGetProcessorShouldReturnCourseProcessorWhenFileTypeIsCourses() {
        assertEquals(courseProcessor, processorStrategy.getFileProcessor("courses"));
    }

    @Test
    public void testGetProcessorShouldReturnEnrollmentProcessorWhenFileTypeIsEnrollments() {
        assertEquals(enrollmentProcessor, processorStrategy.getFileProcessor("enrollments"));
    }

    @Test
    public void testGetProcessorShouldReturnNullWhenFileTypeIsInvalid() {
        assertNull(processorStrategy.getFileProcessor("invalid"));
    }

    @Test
    public void testGetProcessorShouldBeCaseInsensitive() {
        assertEquals(userProcessor, processorStrategy.getFileProcessor("USERS"));
        assertEquals(courseProcessor, processorStrategy.getFileProcessor("CoUrSeS"));
        assertEquals(enrollmentProcessor, processorStrategy.getFileProcessor("EnRoLlMeNtS"));
    }
}