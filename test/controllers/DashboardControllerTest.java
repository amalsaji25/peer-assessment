package controllers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static play.mvc.Http.Status.*;
import static play.mvc.Results.ok;
import static play.test.Helpers.*;

import factory.DashboardFactory;
import java.util.concurrent.CompletableFuture;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Result;
import services.dashboard.Dashboard;

public class DashboardControllerTest {

    private DashboardFactory dashboardFactory;
    private DashboardController controller;

    @Before
    public void setUp() {
        dashboardFactory = mock(DashboardFactory.class);
        controller = new DashboardController(dashboardFactory);
    }

    private Http.Request createRequestWithRole(String role) {
        Http.Session session = new Http.Session(
                java.util.Map.of("role", role)
        );
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(session);
        return request;
    }

    @Test
    public void testDashboard_studentRole_returnsDashboard() throws Exception {
        Http.Request request = createRequestWithRole("student");

        Dashboard studentDashboard = mock(Dashboard.class);
        when(dashboardFactory.getDashboard("student")).thenReturn(studentDashboard);
        when(studentDashboard.dashboard(request)).thenReturn(
                CompletableFuture.completedFuture(ok("Student Dashboard"))
        );

        Result result = controller.dashboard(request).toCompletableFuture().get();
        assertEquals(OK, result.status());
        assertEquals("Student Dashboard", contentAsString(result));
    }

    @Test
    public void testDashboard_professorRole_returnsDashboard() throws Exception {
        Http.Request request = createRequestWithRole("professor");

        Dashboard profDashboard = mock(Dashboard.class);
        when(dashboardFactory.getDashboard("professor")).thenReturn(profDashboard);
        when(profDashboard.dashboard(request)).thenReturn(
                CompletableFuture.completedFuture(ok("Professor Dashboard"))
        );

        Result result = controller.dashboard(request).toCompletableFuture().get();
        assertEquals(OK, result.status());
        assertEquals("Professor Dashboard", contentAsString(result));
    }

    @Test
    public void testDashboard_adminRole_returnsDashboard() throws Exception {
        Http.Request request = createRequestWithRole("admin");

        Dashboard adminDashboard = mock(Dashboard.class);
        when(dashboardFactory.getDashboard("admin")).thenReturn(adminDashboard);
        when(adminDashboard.dashboard(request)).thenReturn(
                CompletableFuture.completedFuture(ok("Admin Dashboard"))
        );

        Result result = controller.dashboard(request).toCompletableFuture().get();
        assertEquals(OK, result.status());
        assertEquals("Admin Dashboard", contentAsString(result));
    }

    @Test
    public void testDashboard_missingRole_returnsUnauthorized() throws Exception {
        Http.Session session = new Http.Session(); // empty session
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenReturn(session);

        Result result = controller.dashboard(request).toCompletableFuture().get();
        assertEquals(UNAUTHORIZED, result.status());
    }

    @Test
    public void testDashboard_invalidRole_redirectsToDashboard() throws Exception {
        Http.Request request = createRequestWithRole("invalidRole");

        Result result = controller.dashboard(request).toCompletableFuture().get();
        assertEquals(SEE_OTHER, result.status()); // redirect
        assertTrue(result.header("Location").orElse("").endsWith("/dashboard"));
    }

    @Test
    public void testDashboard_internalError_redirectsToLogin() throws Exception {
        Http.Request request = mock(Http.Request.class);
        when(request.session()).thenThrow(new RuntimeException("Simulated internal error"));

        Result result = controller.dashboard(request).toCompletableFuture().get();

        assertEquals(SEE_OTHER, result.status());

        String location = result.redirectLocation().orElse("");
        assertTrue("Expected redirect to login but was: " + location, location.contains("/"));
    }

    @Test
    public void testDashboard_factoryThrowsException_returnsUnauthorized() throws Exception {
        Http.Request request = createRequestWithRole("student");
        when(dashboardFactory.getDashboard("student")).thenThrow(new RuntimeException("Error"));

        Result result = controller.dashboard(request).toCompletableFuture().get();
        assertEquals(UNAUTHORIZED, result.status());
    }
}