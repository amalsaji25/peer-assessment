package repository;

import models.Admin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

@Singleton
public class AdminRepository implements Repository<Admin> {

    private final JPAApi jpaApi;
    private static final Logger log = LoggerFactory.getLogger(AdminRepository.class);

    @Inject
    public AdminRepository(JPAApi jpaApi) {
        this.jpaApi = jpaApi;
    }

    public Admin findAdminByUsername(String username) {
        try {
            return jpaApi.withTransaction(entityManager -> {
                List<Admin> admins = entityManager.createQuery(
                                "SELECT a FROM Admin a WHERE a.username = :username", Admin.class)
                        .setParameter("username", username)
                        .getResultList();

                if (admins.isEmpty()) {
                    log.warn("Admin not found for username: {}", username);
                    return null;
                }

                log.info("Found admin with username: {}", username);
                return admins.get(0);
            });
        } catch (Exception e) {
            log.error("Error while finding admin with username {}: {}", username, e.getMessage(), e);
            return null;
        }
    }



    @Override
    public CompletionStage<Map<String, Object>> saveAll(List<Admin> records) {
        return null;
    }
}
