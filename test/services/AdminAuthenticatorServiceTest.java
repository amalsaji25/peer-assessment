package services;

import models.Admin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import play.mvc.Http;
import play.mvc.Result;
import repository.AdminRepository;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static play.mvc.Http.Status.UNAUTHORIZED;
import static play.mvc.Http.RequestBuilder;

@RunWith(MockitoJUnitRunner.class)
public class AdminAuthenticatorServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @InjectMocks
    private AdminAuthenticatorService adminAuthenticatorService;

    private Http.Request request;

    @Before
    public void setup() {
        request = new RequestBuilder().method("GET").build();
    }

    @Test
    public void testGetUsernameShouldReturnEmptyWhenHeadersMissing() {
        Optional<String> username = adminAuthenticatorService.getUsername(request);
        assertTrue(username.isEmpty());
    }

    @Test
    public void testGetUsernameShouldReturnEmptyWhenAdminNotFound() {
        Http.Request req = new RequestBuilder()
                .method("GET")
                .header("X-ADMIN-USERNAME", "admin")
                .header("X-ADMIN-PASSWORD", "password")
                .build();
        when(adminRepository.findAdminByUsername("admin")).thenReturn(null);

        Optional<String> username = adminAuthenticatorService.getUsername(req);

        assertTrue(username.isEmpty());
    }

    @Test
    public void testGetUsernameShouldReturnEmptyWhenPasswordIncorrect() {
        Http.Request req = new RequestBuilder()
                .method("GET")
                .header("X-ADMIN-USERNAME", "admin")
                .header("X-ADMIN-PASSWORD", "wrongpassword")
                .build();

        Admin mockAdmin = new Admin("admin", "correctpassword");

        when(adminRepository.findAdminByUsername("admin")).thenReturn(mockAdmin);

        Optional<String> username = adminAuthenticatorService.getUsername(req);

        assertTrue(username.isEmpty());
    }

    @Test
    public void testGetUsernameShouldReturnAdminUsernameWhenAuthenticationSuccessful() {
        Http.Request req = new RequestBuilder()
                .method("GET")
                .header("X-ADMIN-USERNAME", "admin")
                .header("X-ADMIN-PASSWORD", "correctpassword")
                .build();

        Admin mockAdmin = new Admin("admin", "correctpassword"); // Use constructor

        when(adminRepository.findAdminByUsername("admin")).thenReturn(mockAdmin);

        Optional<String> username = adminAuthenticatorService.getUsername(req);

        assertTrue(username.isPresent());
        assertEquals("admin", username.get());
    }

    @Test
    public void testOnUnauthorizedShouldReturnUnauthorizedResponse() {
        Result result = adminAuthenticatorService.onUnauthorized(request);
        assertEquals(UNAUTHORIZED, result.status());
    }
}
