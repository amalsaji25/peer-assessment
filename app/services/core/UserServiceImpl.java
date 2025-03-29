package services.core;

import models.User;
import repository.core.UserRepository;

import javax.inject.Inject;
import java.util.Optional;

public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;

    @Inject
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> getUserById(Long id){
        return userRepository.findById(id);
    }
}
