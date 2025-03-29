package controllers;

import factory.DashboardFactory;
import play.mvc.*;
import services.AuthenticationService;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Security.Authenticated(AuthenticationService.class)
@Singleton
public class DashboardController extends Controller {

    private final DashboardFactory dashboardFactory;

    @Inject
    public DashboardController(DashboardFactory dashboardFactory) {
        this.dashboardFactory = dashboardFactory;
    }

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
          Results.redirect(routes.AuthController.login().url()).withNewSession()
          );
    }
    }
}
