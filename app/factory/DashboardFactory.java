package factory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import services.dashboard.AdminDashboard;
import services.dashboard.Dashboard;
import services.dashboard.ProfessorDashboard;
import services.dashboard.StudentDashboard;

/**
 * Factory class for creating different types of dashboards based on user roles. It uses dependency
 * injection to provide the necessary dashboard implementations.
 */
public class DashboardFactory {
  Provider<StudentDashboard> studentDashboardProvider;
  Provider<ProfessorDashboard> professorDashboardProvider;
  Provider<AdminDashboard> adminDashboardProvider;

  @Inject
  public DashboardFactory(
      Provider<StudentDashboard> studentDashboardProvider,
      Provider<ProfessorDashboard> professorDashboardProvider,
      Provider<AdminDashboard> adminDashboardProvider) {
    this.studentDashboardProvider = studentDashboardProvider;
    this.professorDashboardProvider = professorDashboardProvider;
    this.adminDashboardProvider = adminDashboardProvider;
  }

  /**
   * Returns the appropriate dashboard implementation based on the user's role.
   *
   * @param role The role of the user (e.g., "student", "professor", "admin").
   * @return The corresponding Dashboard implementation, or null if the role is not recognized.
   */
  public Dashboard getDashboard(String role) {
    return switch (role) {
      case "student" -> studentDashboardProvider.get();
      case "professor" -> professorDashboardProvider.get();
      case "admin" -> adminDashboardProvider.get();
      default -> null;
    };
  }
}
