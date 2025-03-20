package controllers;

import forms.LoginForm;
import models.Users;
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
import services.DashBoardRedirectService;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static play.mvc.Results.*;

@Singleton
public class AuthController {

    private final FormFactory formFactory;
    private final AuthenticationService authenticationService;
    private final DashBoardRedirectService dashBoardRedirectService;
    private static Logger log = LoggerFactory.getLogger(AuthController.class);
    private final CSRF.TokenProvider csrfTokenProvider;
    private final CSRFConfig csrfConfig;

    @Inject
    public AuthController(FormFactory formFactory, AuthenticationService authenticationService, DashBoardRedirectService dashBoardRedirectService, CSRF.TokenProvider csrfTokenProvider, CSRFConfig csrfConfig) {
        this.formFactory = formFactory;
        this.authenticationService = authenticationService;
        this.dashBoardRedirectService = dashBoardRedirectService;
        this.csrfTokenProvider = csrfTokenProvider;
        this.csrfConfig = csrfConfig;
    }

    public Result login(Http.Request request){
        return ok(views.html.index.render(request));
    }

    public Result authenticate(Http.Request request) {

        String csrfCookieName = csrfConfig.cookieName().isDefined() ? csrfConfig.cookieName().get() : "No Cookie Configured";

        log.info("🔍 CSRF Token Key (Header): {}", csrfConfig.headerName());
        log.info("🔍 CSRF Token Key (Cookie): {}", csrfCookieName);

        // Check if it's an already existing session
        Optional<String> existingUserId = request.session().get("userId");
        log.info("Authenticating user {}", existingUserId.orElse("NONE"));
        log.info("Session: {}", request.session().data());
        log.info("Request headers: {}", request.headers().asMap());

        if (existingUserId.isPresent()) {
            log.info("Session already exists for userId: {}", existingUserId.get());

            Optional<CSRF.Token> csrfTokenOpt = CSRF.getToken(request);

            if(csrfTokenOpt.isEmpty()){
                log.warn("CSRF Token is empty! Not setting csrfToken cookie.");
            }

            Result result = ok(Json.toJson(Collections.singletonMap("message", "User already authenticated.")));

            if (csrfTokenOpt.isPresent()) {
                log.info("Setting CSRF Token Cookie: {}", csrfTokenOpt.get().value());
                result = result.withCookies(Http.Cookie.builder("csrfToken", csrfTokenOpt.get().value())
                        .withHttpOnly(false)
                        .withSecure(false)
                        .withSameSite(Http.Cookie.SameSite.LAX)
                        .build());
            } else {
                log.warn("CSRF Token is missing! Not setting csrfToken cookie.");
            }

            return result;
        }

        // Login if session is not found for user
        Form<LoginForm> loginForm = formFactory.form(LoginForm.class).bindFromRequest(request);

        if (loginForm.hasErrors()) {
            log.error("Invalid credentials. Errors: {}", loginForm.errors());
            return badRequest(Json.toJson(Collections.singletonMap("errors", "Invalid credentials. Please try again.")));
        }

        LoginForm loginData = loginForm.get();
        Optional<Users> user = authenticationService.authenticate(loginData.getUserId(), loginData.getPassword());

        if (user.isEmpty()) {
            log.error("Invalid credentials. Please try again.");
            return unauthorized(Json.toJson(Collections.singletonMap("errors", "Invalid credentials. Please try again.")));
        }

        String role = user.get().getRole();
        String dashboardUrl = dashBoardRedirectService.getDashboardUrl(role);

        // Generate CSRF token after authentication
        String csrfToken = csrfTokenProvider.generateToken();

        log.info("Authentication successful for userId: {}. Redirecting to dashboard.", user.get().getUserId());

        return ok(Json.toJson(Collections.singletonMap("redirectUrl", dashboardUrl)))
                .addingToSession(request, "userId", String.valueOf(user.get().getUserId()))
                .addingToSession(request, "role", role)
                .addingToSession(request, "lastActivity", String.valueOf(Instant.now().toEpochMilli()))
                .withHeader("Csrf-Token", csrfToken)
                .withCookies(Http.Cookie.builder("csrfToken", csrfToken)
                        .withHttpOnly(false)
                        .withSecure(false)
                        .withSameSite(Http.Cookie.SameSite.LAX)
                        .build());
    }

    public Result logout(Http.Request request){
        return redirect(routes.AuthController.login()).withNewSession();
    }

}
