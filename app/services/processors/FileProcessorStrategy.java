package services.processors;

import com.google.inject.name.Named;
import models.Course;
import models.Enrollment;
import models.ReviewTask;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class FileProcessorStrategy {

    private final Map<String, FileProcessor<?>> processorMap;
    private static final Logger log = LoggerFactory.getLogger(FileProcessorStrategy.class);

    @Inject
    public FileProcessorStrategy(@Named("users") FileProcessor<User> userProcessor,
                                 @Named("courses") FileProcessor<Course> courseProcessor,
                                 @Named("enrollments") FileProcessor<Enrollment> enrollmentProcessor,
                                 @Named("review_tasks") FileProcessor<ReviewTask> reviewTaskProcessor) {
        processorMap = Map.of(
                "users", userProcessor,
                "courses", courseProcessor,
                "enrollments", enrollmentProcessor,
                "review_tasks", reviewTaskProcessor
        );
    }


    public <T>FileProcessor<T> getProcessor(String fileType) {
        log.info("fileType: {}", fileType);
        return (FileProcessor<T>) processorMap.get(fileType.toLowerCase());
    }
}