package services;

import authorization.Authorization;
import authorization.RoleBasedAuthorization;
import models.enums.Roles;
import play.mvc.Http;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

@Singleton
public class AuthorizationService {

    private final Authorization authorization;

    @Inject
    public AuthorizationService(Authorization authorization) {
        this.authorization = authorization;
    }

    public boolean isAuthorized(Http.Request request, Set<Roles> allowedRoles) {
        return authorization.isAuthorized(request,allowedRoles);
    }

}
