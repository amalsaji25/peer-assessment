package modules;

import authorization.Authorization;
import authorization.RoleBasedAuthorization;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import models.Courses;
import models.Enrollments;
import models.Users;
import repository.CourseRepository;
import repository.EnrollmentRepository;
import repository.Repository;
import repository.UserRepository;
import services.AuthorizationService;
import services.DashBoardRedirectService;
import services.mappers.CourseEntityMapper;
import services.mappers.EnrollmentEntityMapper;
import services.mappers.EntityMapper;
import services.mappers.UserEntityMapper;
import services.processors.CSVProcessor;
import services.processors.FileProcessor;
import services.validations.CourseValidation;
import services.validations.EnrollmentValidation;
import services.validations.UserValidation;
import services.validations.Validations;

public class Module extends AbstractModule {

    @Override
    protected void configure() {
        // Bind UserRepository, CourseRepository, and EnrollmentRepository
        bind(new TypeLiteral<Repository<Users>>() {}).to(UserRepository.class).in(Singleton.class);
        bind(new TypeLiteral<Repository<Courses>>() {}).to(CourseRepository.class).in(Singleton.class);
        bind(new TypeLiteral<Repository<Enrollments>>() {}).to(EnrollmentRepository.class).in(Singleton.class);

        // Bind DashBoardRedirectService
        bind(DashBoardRedirectService.class).asEagerSingleton();

        // Bind Authorization implementations
        bind(Authorization.class).to(RoleBasedAuthorization.class);

        // Bind EntityMapper implementations
        bind(new TypeLiteral<EntityMapper<Users>>() {}).to(UserEntityMapper.class).in(Singleton.class);
        bind(new TypeLiteral<EntityMapper<Courses>>() {}).to(CourseEntityMapper.class).in(Singleton.class);
        bind(new TypeLiteral<EntityMapper<Enrollments>>() {}).to(EnrollmentEntityMapper.class).in(Singleton.class);

        // Bind Validations implementations
        bind(new TypeLiteral<Validations<Users>>() {}).to(UserValidation.class).in(Singleton.class);
        bind(new TypeLiteral<Validations<Courses>>() {}).to(CourseValidation.class).in(Singleton.class);
        bind(new TypeLiteral<Validations<Enrollments>>() {}).to(EnrollmentValidation.class).in(Singleton.class);

        // Bind FileProcessor implementations
        bind(new TypeLiteral<FileProcessor>() {}).annotatedWith(Names.named("users"))
                .to(new TypeLiteral<CSVProcessor<Users>>() {});

        bind(new TypeLiteral<FileProcessor>() {}).annotatedWith(Names.named("courses"))
                .to(new TypeLiteral<CSVProcessor<Courses>>() {});

        bind(new TypeLiteral<FileProcessor>() {}).annotatedWith(Names.named("enrollments"))
                .to(new TypeLiteral<CSVProcessor<Enrollments>>() {});
    }

    @Provides
    @Singleton
    @Named("users")
    public CSVProcessor<Users> provideUsersCSVProcessor(
            Validations<Users> userValidation, // Injected based on the generic type
            EntityMapper<Users> userEntityMapper, // Injected based on the generic type
            UserRepository userRepository) {
        return new CSVProcessor<>(userValidation, userEntityMapper, userRepository);
    }

    @Provides
    @Singleton
    @Named("courses")
    public CSVProcessor<Courses> provideCoursesCSVProcessor(
            Validations<Courses> courseValidation, // Injected based on the generic type
            EntityMapper<Courses> courseEntityMapper, // Injected based on the generic type
            CourseRepository courseRepository) {
        return new CSVProcessor<>(courseValidation, courseEntityMapper, courseRepository);
    }

    @Provides
    @Singleton
    @Named("enrollments")
    public CSVProcessor<Enrollments> provideEnrollmentsCSVProcessor(
            Validations<Enrollments> enrollmentValidation, // Injected based on the generic type
            EntityMapper<Enrollments> enrollmentEntityMapper, // Injected based on the generic type
            EnrollmentRepository enrollmentRepository) {
        return new CSVProcessor<>(enrollmentValidation, enrollmentEntityMapper, enrollmentRepository);
    }
}
