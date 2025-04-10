package services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import javax.inject.Inject;
import models.User;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import repository.core.UserRepository;

/**
 * AuthenticationService is a service class that handles user authentication and session management.
 * It provides methods to validate user credentials, check if the user is logged in, and manage
 * session timeouts.
 */
public class AuthenticationService extends Security.Authenticator {

    private static final long SESSION_TIMEOUT_MINUTES = 30;
    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    private final UserRepository userRepository;

    @Inject
    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Update the session with the current timestamp
     * @param request the HTTP request
     * @return the updated session
     */
    public static Http.Session updateSession(Http.Request request) {
        return request.session().adding("lastActivity", String.valueOf(Instant.now().toEpochMilli()));
    }

    /**
     *  Find if a record exists in the database for the given userId
     * @param userId the ID of the user
     * @return true if the user is logged in, false otherwise
     */
    public Boolean isUserIdValid(Long userId) {
        return userRepository.findById(userId).isPresent();
    }

    /**
     * Set the password for a user with the given userId
     * @param userId the ID of the user
     * @param password the password to set
     * @return true if the password was set successfully, false otherwise
     */
    public boolean setPassword(Long userId, String password) {
        Optional<User> user = userRepository.findById(userId);
        if(user.isPresent()){
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            user.get().setPassword(hashedPassword);
            userRepository.updateUserPassword(user.get());
            return true;
        }
        return false;
    }

    /**
     * Check if the user is logging in for the first time
     * @param userId the ID of the user
     * @return true if it's the user's first time logging in, false otherwise
     */
    public Boolean isFirstTimeLogin(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if(user.isPresent()){
            return user.get().getPassword().isEmpty() || user.get().getPassword().isBlank() || user.get().getPassword() == null;
        }
        return false;
    }

    /**
     * Authenticate the user with the given userId and password
     * @param userId the ID of the user
     * @param password the password to authenticate
     * @return an Optional containing the authenticated User if successful, otherwise empty
     */
    public Optional<User> authenticate(Long userId, String password){
        Optional<User> user = userRepository.findById(userId);
        if(user.isPresent() && BCrypt.checkpw(password, user.get().getPassword())){
            return user; // User Authenticated
        }
        return Optional.empty(); // UnAuthorized User Access
    }

    /**
     * Check if the user is logged in by verifying the session
     * @param request the HTTP request
     * @return an Optional containing the username if logged in, otherwise empty
     */
    @Override
    public Optional<String> getUsername(Http.Request request){
        String sessionUserId = request.session().get("userId").orElse("NONE");
        log.info("SessionId: {}", sessionUserId);
        log.info("Session: {}", request.session().data().toString());
        Optional<String> userId = request.session().get("userId");
        Optional<String> lastActivity = request.session().get("lastActivity");

        if(userId.isEmpty() || lastActivity.isEmpty()){
            return Optional.empty();
        }

        Instant lastActivityTime = Instant.ofEpochMilli(Long.parseLong(lastActivity.get()));
        Instant now = Instant.now();

        if (ChronoUnit.MINUTES.between(lastActivityTime, now) > SESSION_TIMEOUT_MINUTES){
            log.info("Session time expired");
            return Optional.empty();
        }
        log.info("User session is still active");
        return userId;
    }

    /**
     * Check if the user is authorized to access the requested resource
     * @param request the HTTP request
     * @return a Result indicating whether the user is authorized or not
     */
    @Override
    public Result onUnauthorized(Http.Request request) {
        return redirect(controllers.routes.AuthController.login().url())
                .withNewSession();
    }
}
