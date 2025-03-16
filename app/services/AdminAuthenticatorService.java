package services;

import models.Admin;
import org.mindrot.jbcrypt.BCrypt;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import repository.AdminRepository;

import javax.inject.Inject;
import java.util.Optional;

public class AdminAuthenticatorService extends Security.Authenticator {

    private final AdminRepository adminRepository;

    @Inject
    public AdminAuthenticatorService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    public Optional<String> getUsername(Http.Request request){
        Optional<String> usernameFromHeader = request.header("X-ADMIN-USERNAME");
        Optional<String> passwordFromHeader = request.header("X-ADMIN-PASSWORD");

        if(usernameFromHeader.isEmpty() || passwordFromHeader.isEmpty()){
            return Optional.empty();
        }

        String username = usernameFromHeader.get();
        String password = passwordFromHeader.get();

        Admin admin = adminRepository.findAdminByUsername(username);
        if(admin == null){
            return Optional.empty();
        }

        String storedPassword = admin.getPassword();

        if(BCrypt.checkpw(password, storedPassword)){
            return Optional.of(admin.getUsername()); // Admin Authenticated
        }

        return Optional.empty(); // UnAuthorized User Access
    }

    @Override
    public Result onUnauthorized(Http.Request request){
        return unauthorized("Access denied: Unauthorized admin credentials");
    }
}
