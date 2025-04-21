package services.processors;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import models.dto.Context;

/**
 * Processor interface for processing data of type T and input of type I.
 *
 * @param <T> the type of data to be processed
 * @param <I> the type of input data
 */
public interface Processor<T, I> {
  CompletableFuture<List<T>> processData(I input, Context context);

  CompletableFuture<String> saveProcessedData(List<T> processedData, Context context);
}
