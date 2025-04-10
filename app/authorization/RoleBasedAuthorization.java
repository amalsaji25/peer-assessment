package authorization;

import java.util.Set;
import models.enums.Roles;
import play.mvc.Http;

/**
 * RoleBasedAuthorization class implements the Authorization interface. It checks if a user is
 * authorized to perform an action based on their roles.
 */
public class RoleBasedAuthorization implements Authorization {

  /**
   * Checks if the user is authorized to perform an action based on their roles.
   *
   * @param request the incoming HTTP request object containing user context
   * @param allowedRoles list of roles allowed to perform the action
   * @return true if the user is authorized; false otherwise
   */
  @Override
  public boolean isAuthorized(Http.Request request, Set<Roles> allowedRoles) {
    String userRole = getUserRole(request);
    if (userRole == null) {
      return false;
    }
    return allowedRoles.stream().anyMatch(role -> role.name().equalsIgnoreCase(userRole));
  }

  /**
   * Retrieves the user role from the session.
   *
   * @param request the incoming HTTP request object
   * @return the user role as a string, or null if not found
   */
  private String getUserRole(Http.Request request) {
    return request.session().get("role").orElse(null);
  }
}
