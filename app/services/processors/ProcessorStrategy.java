package services.processors;

import com.google.inject.name.Named;
import models.Course;
import models.Enrollment;
import models.ReviewTask;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.processors.record.InputRecord;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.Map;

@Singleton
public class ProcessorStrategy {

    private final Map<String, Processor<?, Path>> fileProcessors;
    private final Map<String, Processor<?, InputRecord>> formProcessors;
    private static final Logger log = LoggerFactory.getLogger(ProcessorStrategy.class);

    @Inject
    public ProcessorStrategy(@Named("users") Processor<User, Path> userProcessor,
                             @Named("courses") Processor<Course, Path> courseProcessor,
                             @Named("enrollments") Processor<Enrollment, Path> enrollmentProcessor,
                             @Named("review_tasks") Processor<ReviewTask, Path> reviewTaskProcessor,
                             @Named("userForm") Processor<User, InputRecord> userFormProcessor,
                             @Named("courseForm") Processor<Course, InputRecord> courseFormProcessor){
        this.fileProcessors = Map.of(
                "users", userProcessor,
                "courses", courseProcessor,
                "enrollments", enrollmentProcessor,
                "review_tasks", reviewTaskProcessor
        );

        this.formProcessors = Map.of(
                "userForm", userFormProcessor,
                "courseForm", courseFormProcessor
        );
    }

    @SuppressWarnings("unchecked")
    public <T> Processor<T, Path> getFileProcessor(String fileType) {
        log.info("Fetching file processor for: {}", fileType);
        return (Processor<T, Path>) fileProcessors.get(fileType.toLowerCase());
    }

    @SuppressWarnings("unchecked")
    public <T> Processor<T, InputRecord> getFormProcessor(String entityType) {
        log.info("Fetching form processor for: {}", entityType);
        return (Processor<T, InputRecord>) formProcessors.get(entityType);
    }
}