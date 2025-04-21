package repository.core;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.User;
import models.dto.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.jpa.JPAApi;

/**
 * UserRepository is a singleton class that handles the persistence of User entities in the database.
 * It provides methods to find users by ID, save users in bulk, update user passwords, and find users
 * by a list of user IDs.
 */
@Singleton
public class UserRepository implements Repository<User> {

    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);
    private final JPAApi jpaApi;
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);


    @Inject
    public  UserRepository(JPAApi jpaApi) {
        this.jpaApi = jpaApi;
    }

    /**
     * Finds a user by their ID.
     *
     * @param userId the ID of the user to find
     * @return an Optional containing the User object if found, or an empty Optional if not found
     */
    public Optional<User> findById(Long userId){
        try{
             return jpaApi.withTransaction(entityManager -> {
                User user = entityManager.find(User.class, userId);
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

    /**
     * Saves a list of users to the database in bulk.
     *
     * @param users   the list of users to save
     * @param context the context containing additional information
     * @return a CompletionStage containing a map with success and failure counts
     */
    @Override
    public CompletionStage<Map<String, Object>> saveAll(List<User> users, Context context) {
        log.info("Starting bulk save of users with size: {}", users.size());
        int batchSize = 100;

        // Split into batches
        List<List<User>> batches = new ArrayList<>();
        for (int i = 0; i < users.size(); i += batchSize) {
            batches.add(users.subList(i, Math.min(i + batchSize, users.size())));
        }

        // Process each batch asynchronously
        List<CompletableFuture<Map<String, Object>>> futures = batches.stream()
                .map(batch -> CompletableFuture.supplyAsync(() -> {
                    return jpaApi.withTransaction(entityManager -> {
                        int successCount = 0;
                        List<Long> failedRecords = new ArrayList<>();

                        for (User user : batch) {
                            try {
                                entityManager.persist(user);
                                successCount++;
                            } catch (Exception e) {
                                failedRecords.add(user.getUserId());
                                System.err.println("Failed to save user ID: " + user.getUserId() + " - " + e.getMessage());
                            }
                        }

                        // Ensure batch commit
                        entityManager.flush();
                        entityManager.clear(); // Helps with memory management

                        // Return structured response
                        Map<String, Object> response = new HashMap<>();
                        response.put("successCount", successCount);
                        response.put("failedCount", failedRecords.size());
                        response.put("failedRecords", failedRecords);
                        return response;
                    });
                }, executorService)) // Run each batch in parallel
                .toList();

        // Wait for all futures to complete and aggregate results
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    int totalSuccess = futures.stream()
                            .mapToInt(f -> (Integer) f.join().get("successCount"))
                            .sum();

                    int totalFailed = futures.stream()
                            .mapToInt(f -> (Integer) f.join().get("failedCount"))
                            .sum();

                    List<String> allFailedRecords = futures.stream()
                            .flatMap(f -> ((List<String>) f.join().get("failedRecords")).stream())
                            .collect(Collectors.toList());

                    Map<String, Object> finalResponse = new HashMap<>();
                    finalResponse.put("successCount", totalSuccess);
                    finalResponse.put("failedCount", totalFailed);
                    finalResponse.put("failedRecords", allFailedRecords);
                    log.info("Bulk save completed for users. Success: {}, Failed: {}", totalSuccess, totalFailed);
                    return finalResponse;
                });
    }


    /**
     * Updates the password of a user in the database.
     *
     * @param userId the user whose password needs to be updated
     */
    public void updateUserPassword(User userId) {
        jpaApi.withTransaction(entityManager -> {
            try {
                User user = entityManager.find(User.class, userId.getUserId());
                if (user != null) {
                    user.setPassword(userId.getPassword());
                    entityManager.merge(user);
                    log.info("User password updated successfully for user ID: {}", userId.getUserId());
                } else {
                    log.warn("User with ID {} not found for password update", userId.getUserId());
                }
            } catch (Exception e) {
                log.error("Failed to update password for user ID {} - {}", userId.getUserId(), e.getMessage());
            }
        });
    }

    /**
     * Finds all users by a list of user IDs.
     *
     * @param list the list of user IDs to find
     * @return a list of User objects
     */
    public List<User> findAllByUserIds(List<Long> list) {
        return jpaApi.withTransaction(entityManager -> {
            List<User> users = entityManager.createQuery("SELECT u FROM User u WHERE u.userId IN :userIds", User.class)
                    .setParameter("userIds", list)
                    .getResultList();
            if(!users.isEmpty()){
                log.info("Users with ids {} found", list);
                return users;
            }
            else{
                log.info("Users with ids {} not found", list);
                return Collections.emptyList();
            }
        });
    }
}
