package services.mappers;

import models.dto.Context;
import services.processors.record.InputRecord;

import java.util.List;


public interface EntityMapper <T>{

    default List<T> mapToEntityList(InputRecord record, Context context){
        return List.of(mapToEntity(record, context));
    }

    T mapToEntity(InputRecord record, Context context);
}
