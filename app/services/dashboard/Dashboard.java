package services.dashboard;

import java.util.concurrent.CompletableFuture;
import play.mvc.Http;
import play.mvc.Result;

/**
 * Dashboard is an interface that defines the contract for dashboard services. It provides a method
 * to retrieve the dashboard view for a given HTTP request. From the request, it extracts the user's
 * role and renders the corresponding dashboard view.
 */
public interface Dashboard {
  CompletableFuture<Result> dashboard(Http.Request request);
}
