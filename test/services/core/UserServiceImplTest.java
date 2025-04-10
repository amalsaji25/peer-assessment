package services.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import models.User;
import models.enums.Roles;
import org.junit.Before;
import org.junit.Test;
import repository.core.UserRepository;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class UserServiceImplTest {

    private UserRepository userRepository;
    private UserServiceImpl userService;

    @Before
    public void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    public void testGetUserById_Found() {
        Long userId = 1L;
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(userId);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    public void testGetUserById_NotFound() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(userId);

        assertFalse(result.isPresent());
    }

    @Test
    public void testValidateProfessor_ReturnsTrueIfProfessor() {
        Long professorId = 2L;
        User user = new User();
        user.setRole(Roles.PROFESSOR.name());

        when(userRepository.findById(professorId)).thenReturn(Optional.of(user));

        CompletableFuture<Boolean> result = userService.validateProfessor(professorId);

        assertTrue(result.join());
    }

    @Test
    public void testValidateProfessor_ReturnsFalseIfNotProfessor() {
        Long professorId = 3L;
        User user = new User();
        user.setRole(Roles.STUDENT.name());

        when(userRepository.findById(professorId)).thenReturn(Optional.of(user));

        CompletableFuture<Boolean> result = userService.validateProfessor(professorId);

        assertFalse(result.join());
    }

    @Test
    public void testValidateProfessor_ReturnsFalseIfUserNotFound() {
        Long professorId = 4L;
        when(userRepository.findById(professorId)).thenReturn(Optional.empty());

        CompletableFuture<Boolean> result = userService.validateProfessor(professorId);

        assertFalse(result.join());
    }
}