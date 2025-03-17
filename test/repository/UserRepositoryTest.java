package repository;

import models.Users;
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
        Users mockUser = mock(Users.class);
        when(mockUser.getUserId()).thenReturn("U123");

        when(mockEntityManager.find(Users.class, "U123")).thenReturn(mockUser);

        Optional<Users> result = userRepository.findById("U123");

        assertTrue(result.isPresent());
        assertEquals("U123", result.get().getUserId());
    }

    /** Test findById() - should return empty if user not found **/
    @Test
    public void testFindByIdShouldReturnEmptyIfNotExists() {
        when(mockEntityManager.find(Users.class, "U999")).thenReturn(null);

        Optional<Users> result = userRepository.findById("U999");

        assertFalse(result.isPresent());
    }

    /** Test findById() - should handle exceptions gracefully **/
    @Test
    public void testFindByIdShouldHandleExceptionGracefully() {
        when(mockEntityManager.find(Users.class, "U123")).thenThrow(new RuntimeException("DB Error"));

        Optional<Users> result = userRepository.findById("U123");

        assertFalse(result.isPresent());
    }

    /** Test saveAll() - should successfully persist all users **/
    @Test
    public void testSaveAllShouldPersistAllUsersSuccessfully() {
        Users mockUser = mock(Users.class);
        Users mockUser1 = mock(Users.class);
        Users mockUser2 = mock(Users.class);
        List<Users> users = Arrays.asList(
                mockUser,
                mockUser1,
                mockUser2
        );

        doNothing().when(mockEntityManager).persist(any(Users.class));

        CompletionStage<Map<String, Object>> resultStage = userRepository.saveAll(users);
        Map<String, Object> result = resultStage.toCompletableFuture().join();

        assertEquals(3, result.get("successCount"));
        assertEquals(0, result.get("failedCount"));
    }

    /** Test saveAll() - should handle failures in persistence **/
    @Test
    public void testSaveAllShouldHandlePersistenceFailures() {
        // Create mock Users objects
        Users user1 = mock(Users.class);
        Users user2 = mock(Users.class);
        Users user3 = mock(Users.class);

        // Define behavior for getUserId() on each mock user
        when(user2.getUserId()).thenReturn("U102");

        // Create a list of users
        List<Users> users = Arrays.asList(user1, user2, user3);

        // Simulate persistence behavior
        doNothing().when(mockEntityManager).persist(user1); // Success
        doThrow(new RuntimeException("DB Error")).when(mockEntityManager).persist(user2); // Failure
        doNothing().when(mockEntityManager).persist(user3); // Success

        // Call the method
        CompletionStage<Map<String, Object>> resultStage = userRepository.saveAll(users);
        Map<String, Object> result = resultStage.toCompletableFuture().join();

        // Validate results
        assertEquals(2, result.get("successCount"));
        assertEquals(1, result.get("failedCount"));
        assertTrue(((List<String>) result.get("failedRecords")).contains("U102"));
    }
}