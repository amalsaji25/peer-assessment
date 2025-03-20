package authorization;

import models.enums.Roles;
import play.mvc.Http;

import java.util.Set;

public interface Authorization {
    boolean isAuthorized(Http.Request request, Set<Roles> allowedRoles);
}
