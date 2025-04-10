package services.core;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import models.User;
import models.enums.Roles;
import repository.core.UserRepository;

/**
 * UserServiceImpl is a service class that implements the UserService interface. It provides methods
 * to interact with the UserRepository for managing user-related operations.
 */
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  @Inject
  public UserServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Retrieves a user by their ID.
   *
   * @param id the ID of the user
   * @return an Optional containing the User if found, otherwise empty
   */
  public Optional<User> getUserById(Long id) {
    return userRepository.findById(id);
  }

  /**
   * Validates if a user is a professor.
   *
   * @param professorId the ID of the professor
   * @return a CompletableFuture containing true if the user is a professor, otherwise false
   */
  @Override
  public CompletableFuture<Boolean> validateProfessor(Long professorId) {

    Optional<User> user = userRepository.findById(professorId);
    if (user.isPresent() && user.get().getRole().equalsIgnoreCase(Roles.PROFESSOR.name())) {
      return CompletableFuture.completedFuture(true);
    } else {
      return CompletableFuture.completedFuture(false);
    }
  }
}
