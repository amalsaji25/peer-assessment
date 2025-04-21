package controllers;

import static play.mvc.Results.*;

import com.fasterxml.jackson.databind.JsonNode;
import forms.LoginForm;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.filters.csrf.CSRF;
import play.filters.csrf.CSRFConfig;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import services.AuthenticationService;

/**
 * AuthController handles authentication-related actions such as login, logout, and user validation.
 * It uses Play Framework's FormFactory for form handling and CSRF protection.
 */
@Singleton
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final FormFactory formFactory;
    private final AuthenticationService authenticationService;
    private final CSRF.TokenProvider csrfTokenProvider;
    private final CSRFConfig csrfConfig;

    @Inject
    public AuthController(FormFactory formFactory, AuthenticationService authenticationService, CSRF.TokenProvider csrfTokenProvider, CSRFConfig csrfConfig) {
        this.formFactory = formFactory;
        this.authenticationService = authenticationService;
        this.csrfTokenProvider = csrfTokenProvider;
        this.csrfConfig = csrfConfig;
    }

    /**
     * Validates the user by checking if the userId exists and if it's the user's first time logging in.
     * @param request the incoming HTTP request object
     * @return a Result indicating whether the user is valid and if it's their first time logging in
     */
    public Result validateUser(Http.Request request) {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest(Json.toJson(Collections.singletonMap("error", "Invalid JSON request")));
        }
        Long userId;
        try{
            userId = json.get("userId").asLong();
        }catch (Exception e){
            return badRequest(Json.toJson(Collections.singletonMap("error", "Invalid userId")));
        }

        if(!authenticationService.isUserIdValid(userId)){
           return ok(Json.toJson(Collections.singletonMap("Invalid user", false)));
        }

        boolean isFirstTimeLogin = authenticationService.isFirstTimeLogin(userId);

        return ok(Json.newObject()
                .put("userExists", true)
                .put("firstTimeUser", isFirstTimeLogin));
    }

    /**
     * Creates a password for the user if the userId is valid.
     * @param request the incoming HTTP request object
     * @return a Result indicating whether the password was created successfully
     */
    public Result createPassword(Http.Request request){
        JsonNode json = request.body().asJson();
        if(json == null || !json.has("userId") || !json.has("password")){
            return badRequest(Json.toJson(Collections.singletonMap("error", "Invalid JSON request - Missing userId or password")));
        }

        Long userId = json.get("userId").asLong();
        String password = json.get("password").asText();

        if(!authenticationService.isUserIdValid(userId)){
            return ok(Json.toJson(Collections.singletonMap("Invalid user", false)));
        }

        boolean setPassword = authenticationService.setPassword(userId, password);
        if(!setPassword){
            return badRequest(Json.toJson(Collections.singletonMap("error", "Failed to set password")));
        }
        return ok(Json.toJson(Collections.singletonMap("Password created", true)));
    }

    /**
     * Handles the login process.
     * If the user is already authenticated, redirects to the dashboard.
     * If not, renders the login page.
     * @param request the incoming HTTP request object
     * @return a Result indicating the outcome of the login process
     */
    public Result login(Http.Request request)
    {
        String csrfCookieName = csrfConfig.cookieName().isDefined() ? csrfConfig.cookieName().get() : "No Cookie Configured";

        log.info("CSRF Token Key (Header): {}", csrfConfig.headerName());
        log.info("CSRF Token Key (Cookie): {}", csrfCookieName);

        // Check if it's an already existing session
        Optional<String> existingUserId = request.session().get("userId");
        log.info("Authenticating user {}", existingUserId.orElse("NONE"));
        log.info("Session: {}", request.session().data());
        log.info("Request headers: {}", request.headers().asMap());

        if (existingUserId.isPresent()) {
            log.info("Session already exists for userId: {}", existingUserId.get());

            String dashboardUrl = routes.DashboardController.dashboard().url();

            Optional<CSRF.Token> csrfTokenOpt = CSRF.getToken(request);

            if(csrfTokenOpt.isEmpty()){
                log.warn("CSRF Token is empty! Not setting csrfToken cookie.");
            }
            log.info("User already authenticated. Redirecting to dashboard: {}", dashboardUrl);
            Result result = ok(Json.toJson(Collections.singletonMap("redirectUrl", dashboardUrl)));

            if (csrfTokenOpt.isPresent()) {
                log.info("Setting CSRF Token Cookie: {}", csrfTokenOpt.get().value());
                 result.withCookies(Http.Cookie.builder("csrfToken", csrfTokenOpt.get().value())
                        .withHttpOnly(false)
                        .withSecure(false)
                        .withSameSite(Http.Cookie.SameSite.LAX)
                        .build());
            } else {
                log.warn("CSRF Token is missing! Not setting csrfToken cookie.");
            }

            return redirect(dashboardUrl).withSession(request.session());
    } else {
            return ok(views.html.index.render(request));
        }
    }

    /**
     * Authenticates the user based on the provided credentials.
     * If successful, redirects to the dashboard and sets session attributes.
     * @param request the incoming HTTP request object
     * @return a Result indicating the outcome of the authentication process
     */
    public Result authenticate(Http.Request request) {

        // Login if session is not found for user
        Form<LoginForm> loginForm = formFactory.form(LoginForm.class).bindFromRequest(request);

        if (loginForm.hasErrors()) {
            log.error("Invalid credentials. Errors: {}", loginForm.errors());
            return badRequest(Json.toJson(Collections.singletonMap("errors", "Invalid credentials. Please try again.")));
        }

        LoginForm loginData = loginForm.get();
        Optional<User> user = authenticationService.authenticate(loginData.getUserId(), loginData.getPassword());

        if (user.isEmpty()) {
            log.error("Invalid credentials. Please try again.");
            return unauthorized(Json.toJson(Collections.singletonMap("errors", "Invalid credentials. Please try again.")));
        }

        String role = user.get().getRole();
        String dashboardUrl = routes.DashboardController.dashboard().url();

        // Generate CSRF token after authentication
        String csrfToken = csrfTokenProvider.generateToken();

        log.info("Authentication successful for userId: {}. Redirecting to dashboard.", user.get().getUserId());

        return ok(Json.toJson(Collections.singletonMap("redirectUrl", dashboardUrl)))
                .addingToSession(request, "userId", String.valueOf(user.get().getUserId()))
                .addingToSession(request, "userName",user.get().getUserName() )
                .addingToSession(request, "role", role)
                .addingToSession(request, "lastActivity", String.valueOf(Instant.now().toEpochMilli()))
                .withHeader("Csrf-Token", csrfToken)
                .withCookies(Http.Cookie.builder("csrfToken", csrfToken)
                        .withHttpOnly(false)
                        .withSecure(false)
                        .withSameSite(Http.Cookie.SameSite.LAX)
                        .build());
    }

    /**
     * Logs out the user by invalidating the session and redirecting to the login page.
     * @param request the incoming HTTP request object
     * @return a Result indicating the outcome of the logout process
     */
    public Result logout(Http.Request request){
        log.info("Logging out.");
        log.info("User Id: {}", request.session().get("userId").orElse("NONE"));
        return redirect(routes.AuthController.login()).withNewSession();
    }

}
