package services.dashboard;

import static play.mvc.Results.ok;

import java.util.concurrent.CompletableFuture;
import javax.inject.Singleton;
import play.mvc.Http;
import play.mvc.Result;
import services.AuthenticationService;

/**
 * AdminDashboard is a service class that implements the Dashboard interface. It provides a method
 * to retrieve the admin dashboard view for a given HTTP request. From the request, it extracts the
 * user's role and renders the corresponding dashboard view.
 */
@Singleton
public class AdminDashboard implements Dashboard {

  /**
   * Retrieves the admin dashboard view for a given HTTP request. From the request, it extracts the
   * user's role and renders the corresponding dashboard view.
   *
   * @param request the HTTP request
   * @return a CompletableFuture containing the Result of the admin dashboard view
   */
  public CompletableFuture<Result> dashboard(Http.Request request) {

    String adminName = "Admin Name";
    String adminRole = "Admin";
    return CompletableFuture.completedFuture(
        ok(views.html.adminDashboard.render(request, adminName, adminRole))
            .withSession(AuthenticationService.updateSession(request)));
  }
}
