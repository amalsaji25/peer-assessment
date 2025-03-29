package services.processors;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface FileProcessor<T> {
    CompletableFuture<List<T>> parseAndProcessFile(Path filePath);
    CompletableFuture<String> saveProcessedFileData(List<T> processedData);
}
