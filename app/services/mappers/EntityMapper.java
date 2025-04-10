package services.mappers;

import java.util.List;
import models.dto.Context;
import services.processors.record.InputRecord;

/**
 * EntityMapper is an interface that defines the contract for mapping input records to entity
 * objects. It provides a method to map a single input record to an entity and a default method to
 * map a list of input records to a list of entities.
 *
 * @param <T> the type of entity to be mapped
 */
public interface EntityMapper<T> {

  default List<T> mapToEntityList(InputRecord record, Context context) {
    return List.of(mapToEntity(record, context));
  }

  T mapToEntity(InputRecord record, Context context);
}
