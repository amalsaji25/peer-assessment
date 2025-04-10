package services.mappers;

import java.util.*;
import javax.inject.Inject;
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

/**
 * ReviewTaskEntityMapper is a service class that implements the EntityMapper interface. It provides
 * a method to map input records to ReviewTask entities. It uses the UserRepository,
 * EnrollmentRepository, and CourseRepository to retrieve the users, enrollments, and course
 * associated with the review task.
 */
public class ReviewTaskEntityMapper implements EntityMapper<ReviewTask> {

  private static final Logger log = LoggerFactory.getLogger(ReviewTaskEntityMapper.class);
  private final UserRepository userRepository;
  private final EnrollmentRepository enrollmentRepository;
  private final CourseRepository courseRepository;

  @Inject
  public ReviewTaskEntityMapper(
      UserRepository userRepository,
      EnrollmentRepository enrollmentRepository,
      CourseRepository courseRepository) {
    this.userRepository = userRepository;
    this.enrollmentRepository = enrollmentRepository;
    this.courseRepository = courseRepository;
  }

  /**
   * Maps an input record to a list of ReviewTask entities. It retrieves the group ID, group name,
   * group size, and user details from the input record. It also fetches the course details and
   * creates new ReviewTask entities for each user in the group.
   *
   * @param record the input record containing review task information
   * @param context the context in which the mapping is performed
   * @return a list of ReviewTask entities mapped from the input record
   */
  @Override
  public List<ReviewTask> mapToEntityList(InputRecord record, Context context) {
    Long groupId = Long.parseLong(record.get("Group ID").trim());
    String groupName = record.get("Group Name").trim();
    int groupSize = Integer.parseInt(record.get("Group Size").trim());

    // Fetch course details
    if (context.getCourseCode() == null
        || context.getCourseSection() == null
        || context.getTerm() == null) {
      throw new IllegalArgumentException(
          "Course code, section, and term must be provided in the context.");
    }

    Optional<Course> course =
        courseRepository.findByCourseCodeAndSectionAndTerm(
            context.getCourseCode(), context.getCourseSection(), context.getTerm());

    if (course.isEmpty()) {
      throw new IllegalArgumentException("Course not found with the provided details.");
    }

    List<User> users = new ArrayList<>();
    List<Enrollment> enrollments = new ArrayList<>();
    for (int i = 1; i <= groupSize; i++) {
      String userIdField = "Member " + i + " ID Number";
      String userFirstNameField = "Member " + i + " Firstname";
      String userLastNameField = "Member " + i + " Lastname";
      String userEmailField = "Member " + i + " Email";
      Long userId = Long.parseLong(record.get(userIdField).trim());
      String userFirstName = record.get(userFirstNameField).trim();
      String userLastName = record.get(userLastNameField).trim();
      String userEmail = record.get(userEmailField).trim();
      User user = new User(userId, userEmail, "", userFirstName, userLastName, "student");
      users.add(user);
    }

    // Fetch existing users
    List<Long> existingUser =
        userRepository.findAllByUserIds(users.stream().map(User::getUserId).toList()).stream()
            .map(User::getUserId)
            .toList();
    log.info("Existing users: " + existingUser.size());

    // Filter out existing users before saving
    List<User> newUsers =
        users.stream().filter(u -> !existingUser.contains(u.getUserId())).toList();
    log.info("New users: " + newUsers.size());

    userRepository.saveAll(newUsers, context).toCompletableFuture().join();

    List<User> savedUsers =
        userRepository.findAllByUserIds(users.stream().map(User::getUserId).toList());
    log.info("Saved users: " + savedUsers.size());

    savedUsers.forEach(
        user -> {
          enrollments.add(
              new Enrollment(user, course.get(), context.getCourseSection(), context.getTerm()));
        });

    // Filter out existing enrollments before saving
    List<Enrollment> existingEnrollments =
        enrollmentRepository.getEnrollmentsByCompositeIndex(
            savedUsers.stream().map(User::getUserId).toList(),
            course.get().getCourseCode(),
            context.getCourseSection(),
            context.getTerm());

    log.info("Initial enrollments: " + enrollments.size());
    log.info("Existing enrollments: " + existingEnrollments.size());

    List<String> existingKeys =
        existingEnrollments.stream()
            .map(
                e ->
                    e.getStudent().getUserId()
                        + "|"
                        + e.getCourse().getCourseCode()
                        + "|"
                        + e.getCourse().getCourseSection()
                        + "|"
                        + e.getCourse().getTerm())
            .toList();

    List<Enrollment> newEnrollments =
        enrollments.stream()
            .filter(
                e ->
                    !existingKeys.contains(
                        e.getStudent().getUserId()
                            + "|"
                            + e.getCourse().getCourseCode()
                            + "|"
                            + e.getCourse().getCourseSection()
                            + "|"
                            + e.getCourse().getTerm()))
            .toList();

    log.info("New enrollments " + newEnrollments.size());
    enrollmentRepository
        .saveAll(newEnrollments, context)
        .thenApply(
            status -> {
              log.info("Success Count: {}", status.get("successCount"));
              log.info("Failed Count: {}", status.get("failedCount"));
              return status;
            })
        .toCompletableFuture()
        .join();

    List<ReviewTask> reviewTasks = new ArrayList<>();
    for (User reviewer : savedUsers) {
      for (User reviewee : savedUsers)
        if (reviewer != null
            && reviewee != null
            && !reviewer.getUserId().equals(reviewee.getUserId())) {
          ReviewTask reviewTask =
              new ReviewTask(
                  null, reviewer, reviewee, Status.PENDING, groupId, groupName, groupSize, false);
          reviewTasks.add(reviewTask);
        }
    }

    // Set private review comment task for professor
    for (User student : savedUsers) {
      ReviewTask reviewTask =
          new ReviewTask(
              null,
              student,
              course.get().getProfessor(),
              Status.PENDING,
              groupId,
              groupName,
              groupSize,
              true);
      reviewTasks.add(reviewTask);
    }
    return reviewTasks;
  }

  /**
   * Maps an input record to a ReviewTask entity. This method is not implemented since it is not
   * required in this context and returns null.
   *
   * @param record the input record containing review task information
   * @param context the context in which the mapping is performed
   * @return null
   */
  @Override
  public ReviewTask mapToEntity(InputRecord record, Context context) {
    return null;
  }
}
