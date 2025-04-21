package modules;

import authorization.Authorization;
import authorization.RoleBasedAuthorization;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import java.nio.file.Path;
import models.Course;
import models.Enrollment;
import models.ReviewTask;
import models.User;
import repository.core.*;
import services.core.*;
import services.dashboard.AdminDashboard;
import services.dashboard.Dashboard;
import services.dashboard.ProfessorDashboard;
import services.dashboard.StudentDashboard;
import services.export.ExcelExportServiceImpl;
import services.export.ExportService;
import services.mappers.*;
import services.processors.CSVProcessor;
import services.processors.FormProcessor;
import services.processors.Processor;
import services.processors.record.InputRecord;
import services.report.ProfessorReportServiceImpl;
import services.report.ReportService;
import services.report.StudentReportServiceImpl;
import services.validations.*;

/**
 * Guice module for binding various services, repositories, and processors in the application. This
 * module is responsible for configuring the dependency injection framework.
 */
public class Module extends AbstractModule {

  /**
   * Configures the bindings for the application. This method is called by Guice to set up the
   * dependency injection.
   */
  @Override
  protected void configure() {
    // Bind UserRepository, CourseRepository, and EnrollmentRepository
    bind(new TypeLiteral<Repository<User>>() {}).to(UserRepository.class).in(Singleton.class);
    bind(new TypeLiteral<Repository<Course>>() {}).to(CourseRepository.class).in(Singleton.class);
    bind(new TypeLiteral<Repository<Enrollment>>() {})
        .to(EnrollmentRepository.class)
        .in(Singleton.class);
    bind(new TypeLiteral<Repository<ReviewTask>>() {})
        .to(ReviewTaskRepository.class)
        .in(Singleton.class);

    // Bind Authorization implementations
    bind(Authorization.class).to(RoleBasedAuthorization.class);

    // Bind EntityMapper implementations
    bind(new TypeLiteral<EntityMapper<User>>() {}).to(UserEntityMapper.class).in(Singleton.class);
    bind(new TypeLiteral<EntityMapper<Course>>() {})
        .to(CourseEntityMapper.class)
        .in(Singleton.class);
    bind(new TypeLiteral<EntityMapper<Enrollment>>() {})
        .to(EnrollmentEntityMapper.class)
        .in(Singleton.class);
    bind(new TypeLiteral<EntityMapper<ReviewTask>>() {})
        .to(ReviewTaskEntityMapper.class)
        .in(Singleton.class);

    // Bind Validations implementations
    bind(new TypeLiteral<Validations<User>>() {}).to(UserValidation.class).in(Singleton.class);
    bind(new TypeLiteral<Validations<Course>>() {}).to(CourseValidation.class).in(Singleton.class);
    bind(new TypeLiteral<Validations<Enrollment>>() {})
        .to(EnrollmentValidation.class)
        .in(Singleton.class);
    bind(new TypeLiteral<Validations<ReviewTask>>() {})
        .to(ReviewTaskValidation.class)
        .in(Singleton.class);

    // Bind Dashboard implementations

    bind(Dashboard.class).to(StudentDashboard.class);
    bind(StudentDashboard.class).asEagerSingleton();
    bind(ProfessorDashboard.class).asEagerSingleton();
    bind(AdminDashboard.class).asEagerSingleton();

    // Bind Core Service implementations

    bind(UserService.class).to(UserServiceImpl.class);
    bind(CourseService.class).to(CourseServiceImpl.class);
    bind(EnrollmentService.class).to(EnrollmentServiceImpl.class);
    bind(AssignmentService.class).to(AssignmentServiceImpl.class);
    bind(ReviewTaskService.class).to(ReviewTaskServiceImpl.class);
    bind(FeedbackService.class).to(FeedbackServiceImpl.class);
    bind(ExportService.class).to(ExcelExportServiceImpl.class);
    bind(ReportService.class)
        .annotatedWith(Names.named("professor"))
        .to(ProfessorReportServiceImpl.class);
    bind(ReportService.class)
        .annotatedWith(Names.named("student"))
        .to(StudentReportServiceImpl.class);
  }

  /**
   * Provides a processor for handling CSV files for users.
   *
   * @param userValidation The validation service for users.
   * @param userEntityMapper The entity mapper for users.
   * @param userRepository The repository for users.
   * @return A processor for handling CSV files for users.
   */
  @Provides
  @Singleton
  @Named("users")
  public Processor<User, Path> provideUsersCSVProcessor(
      Validations<User> userValidation, // Injected based on the generic type
      EntityMapper<User> userEntityMapper, // Injected based on the generic type
      UserRepository userRepository) {
    return new CSVProcessor<>(userValidation, userEntityMapper, userRepository);
  }

  /**
   * Provides a processor for handling CSV files for courses.
   *
   * @param courseValidation The validation service for courses.
   * @param courseEntityMapper The entity mapper for courses.
   * @param courseRepository The repository for courses.
   * @return A processor for handling CSV files for courses.
   */
  @Provides
  @Singleton
  @Named("courses")
  public Processor<Course, Path> provideCoursesCSVProcessor(
      Validations<Course> courseValidation, // Injected based on the generic type
      EntityMapper<Course> courseEntityMapper, // Injected based on the generic type
      CourseRepository courseRepository) {
    return new CSVProcessor<>(courseValidation, courseEntityMapper, courseRepository);
  }

  /**
   * Provides a processor for handling CSV files for enrollments.
   *
   * @param enrollmentValidation The validation service for enrollments.
   * @param enrollmentEntityMapper The entity mapper for enrollments.
   * @param enrollmentRepository The repository for enrollments.
   * @return A processor for handling CSV files for enrollments.
   */
  @Provides
  @Singleton
  @Named("enrollments")
  public Processor<Enrollment, Path> provideEnrollmentsCSVProcessor(
      Validations<Enrollment> enrollmentValidation, // Injected based on the generic type
      EntityMapper<Enrollment> enrollmentEntityMapper, // Injected based on the generic type
      EnrollmentRepository enrollmentRepository) {
    return new CSVProcessor<>(enrollmentValidation, enrollmentEntityMapper, enrollmentRepository);
  }

  /**
   * Provides a processor for handling CSV files for review tasks.
   *
   * @param reviewTaskValidation The validation service for review tasks.
   * @param reviewTaskEntityMapper The entity mapper for review tasks.
   * @param reviewTaskRepository The repository for review tasks.
   * @return A processor for handling CSV files for review tasks.
   */
  @Provides
  @Singleton
  @Named("review_tasks")
  public Processor<ReviewTask, Path> provideReviewTasksCSVProcessor(
      Validations<ReviewTask> reviewTaskValidation, // Injected based on the generic type
      EntityMapper<ReviewTask> reviewTaskEntityMapper, // Injected based on the generic type
      ReviewTaskRepository reviewTaskRepository) {
    return new CSVProcessor<>(reviewTaskValidation, reviewTaskEntityMapper, reviewTaskRepository);
  }

  /**
   * Provides a processor for handling form submissions for users.
   *
   * @param userValidation The validation service for users.
   * @param userEntityMapper The entity mapper for users.
   * @param userRepository The repository for users.
   * @return A processor for handling form submissions for users.
   */
  @Provides
  @Singleton
  @Named("userForm")
  public Processor<User, InputRecord> provideUsersFormProcessor(
      Validations<User> userValidation,
      EntityMapper<User> userEntityMapper,
      UserRepository userRepository) {

    return new FormProcessor<>(userValidation, userEntityMapper, userRepository);
  }

  /**
   * Provides a processor for handling form submissions for courses.
   *
   * @param courseValidation The validation service for courses.
   * @param courseEntityMapper The entity mapper for courses.
   * @param courseRepository The repository for courses.
   * @return A processor for handling form submissions for courses.
   */
  @Provides
  @Singleton
  @Named("courseForm")
  public Processor<Course, InputRecord> provideCoursesFormProcessor(
      Validations<Course> courseValidation,
      EntityMapper<Course> courseEntityMapper,
      CourseRepository courseRepository) {

    return new FormProcessor<>(courseValidation, courseEntityMapper, courseRepository);
  }
}
