package services.mappers;

import org.apache.commons.csv.CSVRecord;
import services.processors.record.InputRecord;

import javax.inject.Singleton;
import java.util.List;


public interface EntityMapper <T>{

    default List<T> mapToEntityList(InputRecord record){
        return List.of(mapToEntity(record));
    }

    T mapToEntity(InputRecord record);
}
