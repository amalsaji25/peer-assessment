package services.dashboard;

import models.Assignment;
import models.dto.AssignmentSummaryDTO;
import models.dto.PeerReviewSummaryDTO;
import play.mvc.Http;
import play.mvc.Result;
import repository.DashboardRepository;
import services.core.AssignmentService;
import services.core.CourseService;
import services.core.EnrollmentService;
import services.core.UserService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static play.mvc.Results.ok;

@Singleton
public class ProfessorDashboard implements Dashboard{

    private final UserService userService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final AssignmentService assignmentService;
    private final DashboardRepository dashboardRepository;


    @Inject
    public ProfessorDashboard(UserService userService,CourseService courseService, EnrollmentService enrollmentService, AssignmentService assignmentService, DashboardRepository dashboardRepository) {
        this.userService = userService;
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.assignmentService = assignmentService;
        this.dashboardRepository = dashboardRepository;
    }

    public CompletableFuture<Result> dashboard(Http.Request request){

        return CompletableFuture.supplyAsync(() -> {
            Long userId = Long.valueOf(request.session().get("userId").get());
            String role = request.session().get("role").get();
            String professorName = userService.getUserById(userId).get().getUserName();
            String courseCode = null;

            if(request.header("courseFilter").isPresent()){
                courseCode = request.header("courseFilter").get();
            }

            CompletableFuture<Integer> studentCountFuture = enrollmentService.getStudentCountByProfessorId(userId, courseCode);
            CompletableFuture<Integer> assignmentCountFuture = assignmentService.getAssignmentCountByProfessorId(userId, courseCode);
            CompletableFuture<Integer> activeCoursesFuture = courseService.getActiveCoursesByProfessorId(userId, courseCode);
            CompletableFuture<List<Assignment>> assignmentsFuture = dashboardRepository.getAssignmentSummaryForProfessor(userId, courseCode);
            CompletableFuture<List<PeerReviewSummaryDTO>> peerReviewAssignmentFuture = dashboardRepository.getPeerReviewProgressForProfessor(userId, courseCode);


            return CompletableFuture.allOf(
                    studentCountFuture,
                    assignmentCountFuture,
                    activeCoursesFuture,
                    assignmentsFuture,
                    peerReviewAssignmentFuture
            ).thenApply(
                    data -> {
                            int studentCount = studentCountFuture.join();
                            int assignmentCount = assignmentCountFuture.join();
                            int activeCourseCount = activeCoursesFuture.join();
                            List<Assignment> assignments = assignmentsFuture.join();
                            List<PeerReviewSummaryDTO> peerReviewAssignments = peerReviewAssignmentFuture.join();

                            return ok(
                                    views.html.professorDashboard.render(
                                            request, professorName, role, studentCount, assignmentCount, activeCourseCount, assignments, peerReviewAssignments
                                    )
                            );
            }).join();
            });
    }
}
