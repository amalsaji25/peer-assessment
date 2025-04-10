package services.processors;

import com.google.inject.name.Named;
import java.nio.file.Path;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.Course;
import models.Enrollment;
import models.ReviewTask;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.processors.record.InputRecord;

/**
 * ProcessorStrategy is a singleton class that manages the retrieval of file and form processors
 * based on the type of data being processed. It provides methods to get the appropriate processor
 * for file and form data.
 */
@Singleton
public class ProcessorStrategy {

  private static final Logger log = LoggerFactory.getLogger(ProcessorStrategy.class);
  private final Map<String, Processor<?, Path>> fileProcessors;
  private final Map<String, Processor<?, InputRecord>> formProcessors;

  @Inject
  public ProcessorStrategy(
      @Named("users") Processor<User, Path> userProcessor,
      @Named("courses") Processor<Course, Path> courseProcessor,
      @Named("enrollments") Processor<Enrollment, Path> enrollmentProcessor,
      @Named("review_tasks") Processor<ReviewTask, Path> reviewTaskProcessor,
      @Named("userForm") Processor<User, InputRecord> userFormProcessor,
      @Named("courseForm") Processor<Course, InputRecord> courseFormProcessor) {
    this.fileProcessors =
        Map.of(
            "users", userProcessor,
            "courses", courseProcessor,
            "enrollments", enrollmentProcessor,
            "review_tasks", reviewTaskProcessor);

    this.formProcessors =
        Map.of(
            "userForm", userFormProcessor,
            "courseForm", courseFormProcessor);
  }

  /**
   * Retrieves the appropriate file processor based on the file type.
   *
   * @param fileType the type of the file (e.g., "users", "courses", etc.)
   * @param <T> the type of entity to be processed
   * @return the processor for the specified file type
   */
  @SuppressWarnings("unchecked")
  public <T> Processor<T, Path> getFileProcessor(String fileType) {
    log.info("Fetching file processor for: {}", fileType);
    return (Processor<T, Path>) fileProcessors.get(fileType.toLowerCase());
  }

  /**
   * Retrieves the appropriate form processor based on the entity type.
   *
   * @param entityType the type of entity (e.g., "userForm", "courseForm", etc.)
   * @param <T> the type of entity to be processed
   * @return the processor for the specified entity type
   */
  @SuppressWarnings("unchecked")
  public <T> Processor<T, InputRecord> getFormProcessor(String entityType) {
    log.info("Fetching form processor for: {}", entityType);
    return (Processor<T, InputRecord>) formProcessors.get(entityType);
  }
}
