package controllers;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.*;

import com.fasterxml.jackson.databind.node.ObjectNode;
import forms.LoginForm;
import models.User;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import play.data.Form;
import play.data.FormFactory;
import play.filters.csrf.CSRF;
import play.filters.csrf.CSRFConfig;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import services.AuthenticationService;

import java.util.*;

public class AuthControllerTest {

    @Mock private FormFactory formFactory;
    @Mock private AuthenticationService authenticationService;
    @Mock private CSRF.TokenProvider csrfTokenProvider;
    @Mock private CSRFConfig csrfConfig;

    @InjectMocks private AuthController authController;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        authController = new AuthController(formFactory, authenticationService, csrfTokenProvider, csrfConfig);
    }

    @Test
    public void testValidateUser_invalidJson_returnsBadRequest() {
        Http.RequestBuilder request = new Http.RequestBuilder().method("POST").bodyText("not-json");
        Result result = authController.validateUser(request.build());
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void testValidateUser_userNotFound_returnsInvalidUser() {
        ObjectNode json = Json.newObject().put("userId", 123L);
        Http.Request request = fakeRequest().bodyJson(json).build();

        when(authenticationService.isUserIdValid(123L)).thenReturn(false);

        Result result = authController.validateUser(request);
        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains("Invalid user"));
    }

    @Test
    public void testValidateUser_userFound_returnsSuccess() {
        ObjectNode json = Json.newObject().put("userId", 123L);
        Http.Request request = fakeRequest().bodyJson(json).build();

        when(authenticationService.isUserIdValid(123L)).thenReturn(true);
        when(authenticationService.isFirstTimeLogin(123L)).thenReturn(true);

        Result result = authController.validateUser(request);
        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains("firstTimeUser"));
    }

    @Test
    public void testCreatePassword_missingFields_returnsBadRequest() {
        ObjectNode json = Json.newObject().put("userId", 1L); // no password
        Http.Request request = fakeRequest().bodyJson(json).build();
        Result result = authController.createPassword(request);
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void testCreatePassword_invalidUser_returnsInvalidUser() {
        ObjectNode json = Json.newObject().put("userId", 1L).put("password", "pass");
        Http.Request request = fakeRequest().bodyJson(json).build();

        when(authenticationService.isUserIdValid(1L)).thenReturn(false);

        Result result = authController.createPassword(request);
        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains("Invalid user"));
    }

    @Test
    public void testCreatePassword_success() {
        ObjectNode json = Json.newObject().put("userId", 1L).put("password", "pass");
        Http.Request request = fakeRequest().bodyJson(json).build();

        when(authenticationService.isUserIdValid(1L)).thenReturn(true);
        when(authenticationService.setPassword(1L, "pass")).thenReturn(true);

        Result result = authController.createPassword(request);
        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains("Password created"));
    }

    @Test
    public void testAuthenticate_invalidForm_returnsBadRequest() {
        Http.Request request = fakeRequest().build();

        Form<LoginForm> form = mock(Form.class);
        when(formFactory.form(LoginForm.class)).thenReturn(form);
        when(form.bindFromRequest(request)).thenReturn(form);
        when(form.hasErrors()).thenReturn(true);
        when(form.errors()).thenReturn(Collections.emptyList());

        Result result = authController.authenticate(request);
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void testAuthenticate_invalidCredentials_returnsUnauthorized() {
        Http.Request request = fakeRequest().build();

        LoginForm loginForm = new LoginForm();
        loginForm.setUserId(1L);
        loginForm.setPassword("wrong");

        Form<LoginForm> form = mock(Form.class);
        when(formFactory.form(LoginForm.class)).thenReturn(form);
        when(form.bindFromRequest(request)).thenReturn(form);
        when(form.hasErrors()).thenReturn(false);
        when(form.get()).thenReturn(loginForm);
        when(authenticationService.authenticate(1L, "wrong")).thenReturn(Optional.empty());

        Result result = authController.authenticate(request);
        assertEquals(UNAUTHORIZED, result.status());
    }

    @Test
    public void testAuthenticate_success_setsSession() {
        Http.Request request = fakeRequest().build();

        LoginForm loginForm = new LoginForm();
        loginForm.setUserId(1L);
        loginForm.setPassword("pass");

        Form<LoginForm> form = mock(Form.class);
        when(formFactory.form(LoginForm.class)).thenReturn(form);
        when(form.bindFromRequest(request)).thenReturn(form);
        when(form.hasErrors()).thenReturn(false);
        when(form.get()).thenReturn(loginForm);


        User user = new User();
        user.setUserId(1L);
        user.setRole("student");

        when(authenticationService.authenticate(1L, "pass")).thenReturn(Optional.of(user));
        when(csrfTokenProvider.generateToken()).thenReturn("csrf123");

        Result result = authController.authenticate(request);
        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains("redirectUrl"));
    }

    @Test
    public void testLogout_clearsSession() {
        Http.Request request = fakeRequest().session("userId", "1").build();
        Result result = authController.logout(request);
        assertEquals(SEE_OTHER, result.status()); // redirect
    }
}