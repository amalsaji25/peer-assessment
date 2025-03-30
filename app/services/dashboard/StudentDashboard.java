package services.dashboard;

import models.Assignment;
import models.dto.FeedbacksReceivedDTO;
import models.dto.ReviewTaskDTO;
import models.enums.Status;
import play.mvc.Http;
import play.mvc.Result;
import repository.DashboardRepository;
import services.core.AssignmentService;
import services.core.FeedbackService;
import services.core.ReviewTaskService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static play.mvc.Results.ok;

@Singleton
public class StudentDashboard implements Dashboard {

    private final AssignmentService assignmentService;
    private final ReviewTaskService reviewTaskService;
    private final FeedbackService feedbackService;
    private final DashboardRepository dashboardRepository;

    @Inject
    public StudentDashboard(AssignmentService assignmentService, ReviewTaskService reviewTaskService, FeedbackService feedbackService, DashboardRepository dashboardRepository) {
        this.assignmentService = assignmentService;
        this.reviewTaskService = reviewTaskService;
        this.feedbackService = feedbackService;
        this.dashboardRepository = dashboardRepository;
    }

    @Override
    public CompletableFuture<Result> dashboard(Http.Request request){
        return CompletableFuture.supplyAsync(() -> {
            Long userId = Long.valueOf(request.session().get("userId").get());
            String role = request.session().get("role").get();
            String studentName = request.session().get("userName").get();

            String courseCode = request.header("courseFilter").orElse(null);

            CompletableFuture<Integer> assignmentCountFuture = assignmentService.getAssignmentCountByStudentId(userId, courseCode);
            CompletableFuture<Integer> pendingReviewsFuture = reviewTaskService.getReviewCountByStatus(userId, courseCode, Status.PENDING);
            CompletableFuture<Integer> completedReviewsFuture = reviewTaskService.getReviewCountByStatus(userId, courseCode, Status.COMPLETED);
            CompletableFuture<List<Assignment>> assignmentsFuture = dashboardRepository.getAssignmentsForStudent(userId, courseCode);
            CompletableFuture<List<ReviewTaskDTO>> peerReviewsFuture = dashboardRepository.getPendingPeerReviewsForStudent(userId, courseCode);
            CompletableFuture<List<FeedbacksReceivedDTO>> myReviewsFuture = feedbackService.getFeedbacksReceivedByStudent(userId, courseCode);

            return CompletableFuture.allOf(
                    assignmentCountFuture,
                    pendingReviewsFuture,
                    completedReviewsFuture,
                    assignmentsFuture,
                    peerReviewsFuture,
                    myReviewsFuture
            ).thenApply(v -> {
                    int assignmentCount = assignmentCountFuture.join();
                    int pendingReviews = pendingReviewsFuture.join();
                    int completedReviews = completedReviewsFuture.join();
                    List<Assignment> assignments = assignmentsFuture.join();
                    List<ReviewTaskDTO> peerReviews = peerReviewsFuture.join();
                    List<FeedbacksReceivedDTO> myReviews = myReviewsFuture.join();

                    return ok(views.html.studentDashboard.render(
                            request, studentName, role, assignmentCount, pendingReviews, completedReviews, assignments, peerReviews, myReviews
                    ));
                }).join();
        });
    }
}
