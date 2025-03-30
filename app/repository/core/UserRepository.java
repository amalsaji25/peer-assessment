package repository.core;

import models.User;
import models.dto.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Singleton
public class UserRepository implements Repository<User> {

    private final JPAApi jpaApi;
    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);


    @Inject
    public  UserRepository(JPAApi jpaApi) {
        this.jpaApi = jpaApi;
    }

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
                .collect(Collectors.toList());

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
