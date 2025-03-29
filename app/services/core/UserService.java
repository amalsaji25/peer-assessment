package services.core;

import models.User;

import java.util.Optional;

public interface UserService {
    Optional<User> getUserById(Long id);
}
