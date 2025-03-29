package services.dashboard;

import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletableFuture;

public interface Dashboard {
    CompletableFuture<Result> dashboard(Http.Request request);
}
