package controllers;

import factory.DashboardFactory;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import play.mvc.*;
import services.AuthenticationService;

/**
 * Controller for handling dashboard-related requests and rendering the appropriate views based on
 * user roles.
 */
@Security.Authenticated(AuthenticationService.class)
@Singleton
public class DashboardController extends Controller {

  private final DashboardFactory dashboardFactory;

  @Inject
  public DashboardController(DashboardFactory dashboardFactory) {
    this.dashboardFactory = dashboardFactory;
  }

  /**
   * Handles the dashboard request and returns the appropriate view based on the user's role.
   *
   * @param request The HTTP request.
   * @return A CompletableFuture containing the Result of the dashboard view.
   */
  public CompletableFuture<Result> dashboard(Http.Request request) {
    try {
      Optional<String> role = request.session().get("role");
      if (role.isEmpty()) {
        return CompletableFuture.completedFuture(unauthorized());
      }
      try {
        return switch (role.get()) {
          case "student", "professor", "admin" ->
              dashboardFactory.getDashboard(role.get()).dashboard(request);

          default ->
              CompletableFuture.completedFuture(
                  Results.redirect(routes.DashboardController.dashboard().url()));
        };
      } catch (Exception e) {
        return CompletableFuture.completedFuture(unauthorized());
      }
    } catch (Exception e) {
      return CompletableFuture.completedFuture(
          Results.redirect(routes.AuthController.login().url()).withNewSession());
    }
  }
}
