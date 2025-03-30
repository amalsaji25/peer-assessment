package services.processors;

import models.dto.Context;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Processor<T,I> {
    CompletableFuture<List<T>> processData(I input, Context context);
    CompletableFuture<String> saveProcessedData(List<T> processedData, Context context);
}
