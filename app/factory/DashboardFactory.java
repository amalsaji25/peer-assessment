package factory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import services.dashboard.AdminDashboard;
import services.dashboard.Dashboard;
import services.dashboard.ProfessorDashboard;
import services.dashboard.StudentDashboard;

public class DashboardFactory {
    Provider<StudentDashboard> studentDashboardProvider;
    Provider<ProfessorDashboard> professorDashboardProvider;
    Provider<AdminDashboard> adminDashboardProvider;

    @Inject
    public DashboardFactory(Provider<StudentDashboard> studentDashboardProvider, Provider<ProfessorDashboard> professorDashboardProvider, Provider<AdminDashboard> adminDashboardProvider) {
        this.studentDashboardProvider = studentDashboardProvider;
        this.professorDashboardProvider = professorDashboardProvider;
        this.adminDashboardProvider = adminDashboardProvider;
    }

    public Dashboard getDashboard(String role) {
        return switch (role) {
            case "student" -> studentDashboardProvider.get();
            case "professor" -> professorDashboardProvider.get();
            case "admin" -> adminDashboardProvider.get();
            default -> null;
        };
    }
}
