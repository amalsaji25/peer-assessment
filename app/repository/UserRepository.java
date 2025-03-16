package repository;

import models.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class UserRepository {

    private final JPAApi jpaApi;
    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);


    @Inject
    public  UserRepository(JPAApi jpaApi) {
        this.jpaApi = jpaApi;
    }

    /* Save user to database*/
    public boolean save(Users user){
        try{
            jpaApi.withTransaction(entityManager -> {
            entityManager.persist(user);
        });
            log.info("save successful for user {}", user.getUserId());
            return true;
            }
        catch (Exception e){
            log.error("save failed for user {} - with exception: {}", user.getUserId(), e.getMessage());
            return false;
        }
    }

    public Optional<Users> findById(String userId){
        try{
             return jpaApi.withTransaction(entityManager -> {
                Users user = entityManager.find(Users.class, userId);
                if(user != null){
                    log.info("User with id {} found", userId);
                    return Optional.of(user);
                }
                else{
                    log.warn("User with id {} not found", userId);
                    return Optional.empty();
                }
            });
        }catch (Exception e) {
            log.error("failed to find user with id {} - with exception: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }
}
