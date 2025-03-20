package services;

import controllers.routes;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class DashBoardRedirectService {

    private final Map<String, String> roleBasedDashboardMap;

    @Inject
    public DashBoardRedirectService() {
        this.roleBasedDashboardMap = new HashMap<>();
    roleBasedDashboardMap.put("admin", routes.AdminController.dashboard().url());
        roleBasedDashboardMap.put("student", routes.StudentController.dashboard().url());
        roleBasedDashboardMap.put("professor", routes.ProfessorController.dashboard().url());
    }

    public String getDashboardUrl(String role) {
        return roleBasedDashboardMap.getOrDefault(role.toLowerCase(),routes.AuthController.login().url());
    }
}
