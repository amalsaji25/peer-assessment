package controllers;

import play.mvc.Result;
import play.mvc.Security;
import repository.AdminRepository;
import services.AdminAuthenticatorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import static play.mvc.Results.internalServerError;
import static play.mvc.Results.ok;

@Security.Authenticated(AdminAuthenticatorService.class)
@Singleton
public class AdminController {

    public final AdminRepository adminRepository;

    @Inject
    public AdminController(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public Result initializeSchema() {
        if (adminRepository.isSchemaInitialized()) {
            return ok("Database schema is correctly initialized.");
        } else {
            return internalServerError("Schema validation failed. Database logs required.");
        }
    }


}
