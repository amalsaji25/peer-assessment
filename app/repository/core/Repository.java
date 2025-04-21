package repository.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import models.dto.Context;

/**
 * Repository interface for saving records to the database.
 *
 * @param <T> the type of records to be saved
 */
public interface Repository<T> {

  /**
   * Saves a list of records to the database.
   *
   * @param records the list of records to be saved
   * @param context the context containing additional information
   * @return a CompletionStage containing a map with success and failure counts
   */
  CompletionStage<Map<String, Object>> saveAll(List<T> records, Context context);
}
