package services;

import authorization.Authorization;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.enums.Roles;
import play.mvc.Http;

/**
 * AuthorizationService is a service class that handles authorization logic. It checks if a user is
 * authorized to access certain resources based on their roles.
 */
@Singleton
public class AuthorizationService {

  private final Authorization authorization;

  @Inject
  public AuthorizationService(Authorization authorization) {
    this.authorization = authorization;
  }

  /**
   * Checks if the user is authorized to access a resource based on their roles.
   *
   * @param request the HTTP request
   * @param allowedRoles the set of roles that are allowed to access the resource
   * @return true if the user is authorized, false otherwise
   */
  public boolean isAuthorized(Http.Request request, Set<Roles> allowedRoles) {
    return authorization.isAuthorized(request, allowedRoles);
  }
}
