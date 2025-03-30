package services.processors;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Processor<T,I> {
    CompletableFuture<List<T>> processData(I input);
    CompletableFuture<String> saveProcessedData(List<T> processedData);
}
