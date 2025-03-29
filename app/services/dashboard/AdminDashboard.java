package services.dashboard;

import play.mvc.Http;
import play.mvc.Result;
import services.AuthenticationService;

import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;

import static play.mvc.Results.ok;

@Singleton
public class AdminDashboard implements Dashboard{

    public CompletableFuture<Result> dashboard(Http.Request request){

        String adminName = "Admin Name";
        String adminRole = "Admin";
        return CompletableFuture.completedFuture(ok(views.html.adminDashboard.render(
                request, adminName, adminRole
        )).withSession(AuthenticationService.updateSession(request)));
    }
}
