package services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import play.mvc.Http;
import play.mvc.Result;

import java.time.Instant;
import java.util.Optional;

import static org.junit.Assert.*;
import static play.mvc.Http.Status.UNAUTHORIZED;
import static play.mvc.Http.RequestBuilder;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    private Http.Request request;

    @Before
    public void setup() {
        request = new RequestBuilder().method("GET").build();
    }

    @Test
    public void testGetUsernameShouldReturnEmptyWhenHeadersMissing() {
        Optional<String> username = authenticationService.getUsername(request);
        assertTrue(username.isEmpty());
    }

    @Test
    public void testGetUsernameShouldReturnEmptyWhenUserIdNotFound() {
        Http.Request req = new RequestBuilder()
                .method("GET")
                .session("lastActivity", String.valueOf(Instant.now().toEpochMilli()))
                .build();

        Optional<String> username = authenticationService.getUsername(req);

        assertTrue(username.isEmpty());
    }

    @Test
    public void testGetUsernameShouldReturnEmptyWhenLastActivityNotFound() {
        Http.Request req = new RequestBuilder()
                .method("GET")
                .session("userId", "0001")
                .build();

        Optional<String> username = authenticationService.getUsername(req);

        assertTrue(username.isEmpty());
    }

    @Test
    public void testGetUsernameShouldReturnUserIdWhenAuthenticationSuccessful() {
        Http.Request req = new RequestBuilder()
                .method("GET")
                .session("userId", "0001")
                .session("lastActivity", String.valueOf(Instant.now().toEpochMilli()))
                .build();

        Optional<String> userId = authenticationService.getUsername(req);

        assertTrue(userId.isPresent());
        assertEquals(0001L, Long.parseLong(userId.get()));
    }

    @Test
    public void testOnUnauthorizedShouldReturnUnauthorizedResponse() {
        Result result = authenticationService.onUnauthorized(request);
        assertEquals(UNAUTHORIZED, result.status());
    }
}
