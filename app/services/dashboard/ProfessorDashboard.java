package services.dashboard;

import static play.mvc.Results.ok;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.Assignment;
import models.dto.PeerReviewSummaryDTO;
import play.mvc.Http;
import play.mvc.Result;
import repository.DashboardRepository;
import services.core.AssignmentService;
import services.core.CourseService;
import services.core.EnrollmentService;
import services.core.UserService;

/**
 * ProfessorDashboard is a service class that implements the Dashboard interface. It provides a
 * method to retrieve the professor dashboard view for a given HTTP request. From the request, it
 * extracts the user's role and renders the corresponding dashboard view.
 */
@Singleton
public class ProfessorDashboard implements Dashboard {

  private final UserService userService;
  private final CourseService courseService;
  private final EnrollmentService enrollmentService;
  private final AssignmentService assignmentService;
  private final DashboardRepository dashboardRepository;

  @Inject
  public ProfessorDashboard(
      UserService userService,
      CourseService courseService,
      EnrollmentService enrollmentService,
      AssignmentService assignmentService,
      DashboardRepository dashboardRepository) {
    this.userService = userService;
    this.courseService = courseService;
    this.enrollmentService = enrollmentService;
    this.assignmentService = assignmentService;
    this.dashboardRepository = dashboardRepository;
  }

    /**
     * Converts a Java List to a Scala immutable List.
     * @param javaList the Java List to convert
     * @return the converted Scala immutable List
     * @param <T> the type of elements in the list
     */

    private static <T> scala.collection.immutable.List<T> toScalaImmutableList(java.util.List<T> javaList) {
        return scala.jdk.javaapi.CollectionConverters.asScala(javaList).toList();
    }

  /**
   * Retrieves the professor dashboard view for a given HTTP request. From the request, it extracts
   * the user's role and renders the corresponding dashboard view.
   *
   * @param request the HTTP request
   * @return a CompletableFuture containing the Result of the professor dashboard view
   */
  public CompletableFuture<Result> dashboard(Http.Request request) {

    return CompletableFuture.supplyAsync(
        () -> {
          Long userId = Long.valueOf(request.session().get("userId").get());
          String role = request.session().get("role").get();
          String professorName = userService.getUserById(userId).get().getUserName();
          String courseCode = null;
          String courseSection = null;
          String term = null;

          if (request.header("courseFilter").isPresent()
              && request.header("termFilter").isPresent()) {
            courseCode = request.header("courseFilter").get().split(":::")[0].trim();
            courseSection = request.header("courseFilter").get().split(":::")[1].trim();
            term = request.header("termFilter").get();
          }

          CompletableFuture<Integer> studentCountFuture =
              enrollmentService.getStudentCountByProfessorId(
                  userId, courseCode, courseSection, term);
          CompletableFuture<Integer> assignmentCountFuture =
              assignmentService.getAssignmentCountByProfessorId(
                  userId, courseCode, courseSection, term);
          CompletableFuture<Integer> activeCoursesFuture =
              courseService.getActiveCoursesByProfessorId(userId, courseCode, courseSection, term);
          CompletableFuture<List<Assignment>> assignmentsFuture =
              dashboardRepository.getAssignmentSummaryForProfessor(
                  userId, courseCode, courseSection, term);
          CompletableFuture<List<PeerReviewSummaryDTO>> peerReviewAssignmentFuture =
              dashboardRepository.getPeerReviewProgressForProfessor(
                  userId, courseCode, courseSection, term);

          return CompletableFuture.allOf(
                  studentCountFuture,
                  assignmentCountFuture,
                  activeCoursesFuture,
                  assignmentsFuture,
                  peerReviewAssignmentFuture)
              .thenApply(
                  data -> {
                    int studentCount = studentCountFuture.join();
                    int assignmentCount = assignmentCountFuture.join();
                    int activeCourseCount = activeCoursesFuture.join();
                    List<Assignment> assignments = assignmentsFuture.join();
                    List<PeerReviewSummaryDTO> peerReviewAssignments =
                        peerReviewAssignmentFuture.join();

                    return ok(
                        views.html.professorDashboard.render(
                            request,
                            professorName,
                            role,
                            studentCount,
                            assignmentCount,
                            activeCourseCount,
                            toScalaImmutableList(assignments),
                            toScalaImmutableList(peerReviewAssignments)));
                  })
              .join();
        });
  }
}
