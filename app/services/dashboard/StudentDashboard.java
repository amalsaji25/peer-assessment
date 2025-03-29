package services.dashboard;

import models.Assignment;
import models.Feedback;
import models.ReviewTask;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static play.mvc.Results.ok;

@Singleton
public class StudentDashboard implements Dashboard {

    @Override
    public CompletableFuture<Result> dashboard(Http.Request request){
        String studentName = "John Smith";
        String studentRole = "Student";

        // Initial values (default counts and empty lists)
        int assignmentCount = 0;
        int pendingReviews = 0;
        int completedReviews = 0;
        List<Assignment> assignments = Collections.emptyList();  // Empty list
        List<ReviewTask> peerReviews = Collections.emptyList();  // Empty list
        List<Feedback> myReviews = Collections.emptyList();      // Empty list

        return CompletableFuture.completedFuture(ok(views.html.studentDashboard.render(
                request, studentName, studentRole, assignmentCount, pendingReviews, completedReviews, assignments, peerReviews, myReviews
        )));
    }
}
