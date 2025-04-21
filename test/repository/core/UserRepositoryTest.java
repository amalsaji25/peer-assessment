package repository.core;

import models.User;
import models.dto.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import play.db.jpa.JPAApi;

import jakarta.persistence.EntityManager;

import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserRepositoryTest {

    @Mock
    private JPAApi mockJPAApi;

    @Mock
    private Context mockContext;

    @Mock
    private EntityManager mockEntityManager;

    private UserRepository userRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(mockJPAApi.withTransaction(any(Function.class)))
                .thenAnswer(invocation -> {
                    Function<EntityManager, Object> function = invocation.getArgument(0);
                    return function.apply(mockEntityManager);
                });

        userRepository = new UserRepository(mockJPAApi);
    }

    /** Test findById() - should return user if found **/
    @Test
    public void testFindByIdShouldReturnUserIfExists() {
        User mockUser = mock(User.class);
        when(mockUser.getUserId()).thenReturn(123L);

        when(mockEntityManager.find(User.class, 123L)).thenReturn(mockUser);

        Optional<User> result = userRepository.findById(123L);

        assertTrue(result.isPresent());
        assertEquals(Long.valueOf(123L), result.get().getUserId());
    }

    /** Test findById() - should return empty if user not found **/
    @Test
    public void testFindByIdShouldReturnEmptyIfNotExists() {
        when(mockEntityManager.find(User.class, 999L)).thenReturn(null);

        Optional<User> result = userRepository.findById(999L);

        assertFalse(result.isPresent());
    }

    /** Test findById() - should handle exceptions gracefully **/
    @Test
    public void testFindByIdShouldHandleExceptionGracefully() {
        when(mockEntityManager.find(User.class, 123L)).thenThrow(new RuntimeException("DB Error"));

        Optional<User> result = userRepository.findById(123L);

        assertFalse(result.isPresent());
    }

    /** Test saveAll() - should successfully persist all users **/
    @Test
    public void testSaveAllShouldPersistAllUsersSuccessfully() {
        User mockUser = mock(User.class);
        User mockUser1 = mock(User.class);
        User mockUser2 = mock(User.class);
        List<User> users = Arrays.asList(
                mockUser,
                mockUser1,
                mockUser2
        );

        doNothing().when(mockEntityManager).persist(any(User.class));

        CompletionStage<Map<String, Object>> resultStage = userRepository.saveAll(users, mockContext);
        Map<String, Object> result = resultStage.toCompletableFuture().join();

        assertEquals(3, result.get("successCount"));
        assertEquals(0, result.get("failedCount"));
    }

    /** Test saveAll() - should handle failures in persistence **/
    @Test
    public void testSaveAllShouldHandlePersistenceFailures() {
        // Create mock User objects
        User user1 = mock(User.class);
        User user2 = mock(User.class);
        User user3 = mock(User.class);

        // Define behavior for getUserId() on each mock user
        when(user2.getUserId()).thenReturn(123L);

        // Create a list of users
        List<User> users = Arrays.asList(user1, user2, user3);

        // Simulate persistence behavior
        doNothing().when(mockEntityManager).persist(user1); // Success
        doThrow(new RuntimeException("DB Error")).when(mockEntityManager).persist(user2); // Failure
        doNothing().when(mockEntityManager).persist(user3); // Success

        // Call the method
        CompletionStage<Map<String, Object>> resultStage = userRepository.saveAll(users, mockContext);
        Map<String, Object> result = resultStage.toCompletableFuture().join();

        // Validate results
        assertEquals(2, result.get("successCount"));
        assertEquals(1, result.get("failedCount"));
        assertTrue(((List<String>) result.get("failedRecords")).contains(123L));
    }
}