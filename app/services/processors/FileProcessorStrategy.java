package services.processors;

import com.google.inject.name.Named;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class FileProcessorStrategy {

    private final Map<String, FileProcessor> processorMap;

    @Inject
    public FileProcessorStrategy(@Named("users") FileProcessor userProcessor,
                                 @Named("courses") FileProcessor courseProcessor,
                                 @Named("enrollments") FileProcessor enrollmentProcessor) {
        processorMap = Map.of(
                "users", userProcessor,
                "courses", courseProcessor,
                "enrollments", enrollmentProcessor
        );
    }

    public FileProcessor getProcessor(String fileType) {
        return processorMap.getOrDefault(fileType.toLowerCase(), null);
    }
}