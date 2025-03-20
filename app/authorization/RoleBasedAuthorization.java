package authorization;

import models.enums.Roles;
import play.mvc.Http;

import java.util.Set;

public class RoleBasedAuthorization implements Authorization {

    @Override
    public boolean isAuthorized(Http.Request request, Set<Roles> allowedRoles) {
            String userRole = getUserRole(request);
            if(userRole == null) {
                return false;
            }
            return allowedRoles.stream().anyMatch(role -> role.name().equalsIgnoreCase(userRole));
    }

    private String getUserRole(Http.Request request) {
        return request.session().get("role").orElse(null);
    }
}
