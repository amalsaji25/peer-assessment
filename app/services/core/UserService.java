package services.core;

import models.User;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface UserService {
    Optional<User> getUserById(Long id);

    CompletableFuture<Boolean> validateProfessor(Long professorId);
}
