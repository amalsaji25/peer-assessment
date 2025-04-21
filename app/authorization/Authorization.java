package authorization;

import java.util.Set;
import models.enums.Roles;
import play.mvc.Http;

/**
 * Interface for authorization checks. This interface defines a method to check if a user is
 * authorized to perform an action based on their roles.
 */
public interface Authorization {
  /**
   * Checks if the user is authorized to perform an action based on their roles.
   *
   * @param request the incoming HTTP request object containing user context
   * @param allowedRoles list of roles allowed to perform the action
   * @return true if the user is authorized; false otherwise
   */
  boolean isAuthorized(Http.Request request, Set<Roles> allowedRoles);
}
