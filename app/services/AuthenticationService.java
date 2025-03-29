package services;

import models.User;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import repository.core.UserRepository;

import javax.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class AuthenticationService extends Security.Authenticator {

    private final UserRepository userRepository;

    private static final long SESSION_TIMEOUT_MINUTES = 30;

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    @Inject
    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> authenticate(Long userId, String password){
        Optional<User> user = userRepository.findById(userId);
        if(user.isPresent() && BCrypt.checkpw(password, user.get().getPassword())){
            return user; // User Authenticated
        }
        return Optional.empty(); // UnAuthorized User Access
    }

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

    @Override
    public Result onUnauthorized(Http.Request request) {
        return redirect(controllers.routes.AuthController.login().url())
                .withNewSession();
    }

    public static Http.Session updateSession(Http.Request request) {
        return request.session().adding("lastActivity", String.valueOf(Instant.now().toEpochMilli()));
    }
}
