package services.mappers;

import models.Course;
import models.Enrollment;
import models.ReviewTask;
import models.User;
import models.dto.Context;
import models.enums.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.core.CourseRepository;
import repository.core.EnrollmentRepository;
import repository.core.UserRepository;
import services.processors.record.InputRecord;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

public class ReviewTaskEntityMapper implements EntityMapper<ReviewTask> {

    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final static Logger log = LoggerFactory.getLogger(ReviewTaskEntityMapper.class);

    @Inject
    public ReviewTaskEntityMapper(UserRepository userRepository, EnrollmentRepository enrollmentRepository, CourseRepository courseRepository) {
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public List<ReviewTask> mapToEntityList(InputRecord record, Context context) {
        Long groupId = Long.parseLong(record.get("Group ID").trim());
        String groupName = record.get("Group Name").trim();
        int groupSize = Integer.parseInt(record.get("Group Size").trim());

        // Fetch course details
        if(context.getCourseCode() == null || context.getCourseSection() == null || context.getTerm() == null) {
            throw new IllegalArgumentException("Course code, section, and term must be provided in the context.");
        }

        Optional<Course> course = courseRepository.findByCourseCodeAndSectionAndTerm(
                context.getCourseCode(),
                context.getCourseSection(),
                context.getTerm()
        );

        if(course.isEmpty()) {
            throw new IllegalArgumentException("Course not found with the provided details.");
        }

        List<User> users = new ArrayList<>();
        List<Enrollment> enrollments = new ArrayList<>();
        for(int i = 1; i <= groupSize; i++) {
            String userIdField = "Member " + i + " ID Number";
            String userFirstNameField = "Member " + i + " Firstname";
            String userLastNameField = "Member " + i + " Lastname";
            String userEmailField = "Member " + i + " Email";
            Long userId = Long.parseLong(record.get(userIdField).trim());
            String userFirstName = record.get(userFirstNameField).trim();
            String userLastName = record.get(userLastNameField).trim();
            String userEmail = record.get(userEmailField).trim();
            User user = new User(userId,userEmail, "", userFirstName,userLastName,"student");
            users.add(user);
        }

        // Fetch existing users
        List<Long> existingUser = userRepository.findAllByUserIds(users.stream().map(User::getUserId).toList())
                .stream()
                .map(User::getUserId)
                .toList();
        log.info("Existing users: " + existingUser.size());

        // Filter out existing users before saving
        List<User> newUsers = users.stream()
                .filter(u -> !existingUser.contains(u.getUserId()))
                .toList();
        log.info("New users: " + newUsers.size());

        userRepository.saveAll(newUsers, context).toCompletableFuture().join();

        List<User> savedUsers = userRepository.findAllByUserIds(users.stream().map(User::getUserId).toList());
        log.info("Saved users: " + savedUsers.size());

        savedUsers.forEach(user -> {
            enrollments.add(new Enrollment(user,course.get(),context.getCourseSection(),context.getTerm()));
        });

        // Filter out existing enrollments before saving
        List<Enrollment> existingEnrollments = enrollmentRepository.getEnrollmentsByCompositeIndex(
                savedUsers.stream().map(User::getUserId).toList(),
                course.get().getCourseCode(),
                context.getCourseSection(),
                context.getTerm()
        );


        log.info("Intial enrollments: " + enrollments.size());
        log.info("Existing enrollments: " + existingEnrollments.size());

        List<String> existingKeys = existingEnrollments.stream()
                .map(e -> e.getStudent().getUserId() + "|"
                        + e.getCourse().getCourseCode() + "|"
                        + e.getCourse().getCourseSection() + "|"
                        + e.getCourse().getTerm())
                .toList();

        List<Enrollment> newEnrollments = enrollments.stream()
                .filter(e -> !existingKeys.contains(
                        e.getStudent().getUserId() + "|"
                                + e.getCourse().getCourseCode() + "|"
                                + e.getCourse().getCourseSection() + "|"
                                + e.getCourse().getTerm()))
                .toList();

        log.info("New enrollments " + newEnrollments.size());
        enrollmentRepository.saveAll(newEnrollments, context)
                .thenApply(status -> {
                    log.info("Success Count: {}", status.get("successCount"));
                    log.info("Failed Count: {}", status.get("failedCount"));
                    return status;
                }).toCompletableFuture().join();

        List<ReviewTask> reviewTasks = new ArrayList<>();
        for(User reviewer : savedUsers) {
            for(User reviewee : savedUsers)
                if(reviewer != null && reviewee != null && !reviewer.getUserId().equals(reviewee.getUserId())){
                    ReviewTask reviewTask = new ReviewTask(
                            null,
                            reviewer,
                            reviewee,
                            Status.PENDING,
                            groupId,
                            groupName,
                            groupSize
                    );
                    reviewTasks.add(reviewTask);
                }
        }
        return reviewTasks;
    }

    @Override
    public ReviewTask mapToEntity(InputRecord record, Context context) {
        return null;
    }
}
