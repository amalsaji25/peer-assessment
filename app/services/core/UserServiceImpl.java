package services.core;

import models.User;
import models.enums.Roles;
import repository.core.UserRepository;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;

    @Inject
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> getUserById(Long id){
        return userRepository.findById(id);
    }

    @Override
    public CompletableFuture<Boolean> validateProfessor(Long professorId) {

        Optional<User> user = userRepository.findById(professorId);
        if(user.isPresent() && user.get().getRole().equalsIgnoreCase(Roles.PROFESSOR.name())) {
            return CompletableFuture.completedFuture(true);
        } else {
            return CompletableFuture.completedFuture(false);
        }
    }
}
