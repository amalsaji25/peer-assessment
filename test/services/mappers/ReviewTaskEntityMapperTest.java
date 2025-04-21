package services.mappers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import models.*;
import models.dto.Context;
import models.enums.Status;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import repository.core.CourseRepository;
import repository.core.EnrollmentRepository;
import repository.core.UserRepository;
import services.processors.record.InputRecord;

public class ReviewTaskEntityMapperTest {

    @Mock private UserRepository userRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private EnrollmentRepository enrollmentRepository;

    @InjectMocks private ReviewTaskEntityMapper mapper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mapper = new ReviewTaskEntityMapper(userRepository, enrollmentRepository, courseRepository);
    }

    @Test
    public void testMapToEntityList_Success() {
        // Setup input record
        InputRecord input = mock(InputRecord.class);
        when(input.get("Group ID")).thenReturn("101");
        when(input.get("Group Name")).thenReturn("Alpha");
        when(input.get("Group Size")).thenReturn("2");
        when(input.get("Member 1 ID Number")).thenReturn("1");
        when(input.get("Member 1 Firstname")).thenReturn("John");
        when(input.get("Member 1 Lastname")).thenReturn("Doe");
        when(input.get("Member 1 Email")).thenReturn("john@example.com");
        when(input.get("Member 2 ID Number")).thenReturn("2");
        when(input.get("Member 2 Firstname")).thenReturn("Jane");
        when(input.get("Member 2 Lastname")).thenReturn("Smith");
        when(input.get("Member 2 Email")).thenReturn("jane@example.com");

        Context context = new Context();
        context.setCourseCode("CS101");
        context.setCourseSection("A");
        context.setTerm("Fall 2025");

        User prof = new User(999L, "prof@example.com", "", "Prof", "X", "professor");
        Course mockCourse = new Course("CS101", "Some Course", prof, "Fall 2025", "A", false);

        when(courseRepository.findByCourseCodeAndSectionAndTerm("CS101", "A", "Fall 2025"))
                .thenReturn(Optional.of(mockCourse));

        List<User> emptyList = new ArrayList<>();
        when(userRepository.findAllByUserIds(Arrays.asList(1L, 2L))).thenReturn(emptyList);
        when(userRepository.saveAll(anyList(), eq(context)))
                .thenReturn(CompletableFuture.completedFuture(null));

        // After saveAll, simulate they are now found
        User user1 = new User(1L, "john@example.com", "", "John", "Doe", "student");
        User user2 = new User(2L, "jane@example.com", "", "Jane", "Smith", "student");
        when(userRepository.findAllByUserIds(Arrays.asList(1L, 2L)))
                .thenReturn(Arrays.asList(user1, user2));

        when(enrollmentRepository.getEnrollmentsByCompositeIndex(anyList(), eq("CS101"), eq("A"), eq("Fall 2025")))
                .thenReturn(new ArrayList<>());

        when(enrollmentRepository.saveAll(anyList(), eq(context)))
                .thenReturn(CompletableFuture.completedFuture(Map.of("successCount", 2, "failedCount", 0)));

        List<ReviewTask> result = mapper.mapToEntityList(input, context);

        // 2 students → 2x1 review tasks (excluding self-review) + 2 tasks for professor
        assertEquals(4, result.size());

        for (ReviewTask task : result) {
            assertEquals("Alpha", task.getGroupName());
            assertEquals(Long.valueOf(101), task.getGroupId());
            assertEquals(2, task.getGroupSize());
            assertEquals(Status.PENDING, task.getStatus());
        }

        assertTrue(result.stream().anyMatch(rt -> rt.isReviewTaskForProfessor()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMapToEntityList_ThrowsOnMissingCourse() {
        Context context = new Context();
        context.setCourseCode("CS101");
        context.setCourseSection("A");
        context.setTerm("Fall 2025");

        when(courseRepository.findByCourseCodeAndSectionAndTerm("CS101", "A", "Fall 2025"))
                .thenReturn(Optional.empty());

        InputRecord input = mock(InputRecord.class);
        when(input.get("Group ID")).thenReturn("101");
        when(input.get("Group Name")).thenReturn("Alpha");
        when(input.get("Group Size")).thenReturn("1");
        when(input.get("Member 1 ID Number")).thenReturn("1");
        when(input.get("Member 1 Firstname")).thenReturn("John");
        when(input.get("Member 1 Lastname")).thenReturn("Doe");
        when(input.get("Member 1 Email")).thenReturn("john@example.com");

        mapper.mapToEntityList(input, context); // should throw
    }
}