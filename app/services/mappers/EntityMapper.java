package services.mappers;

import org.apache.commons.csv.CSVRecord;

import javax.inject.Singleton;
import java.util.List;


public interface EntityMapper <T>{

    default List<T> mapToEntityList(CSVRecord record){
        return List.of(mapToEntity(record));
    }

    T mapToEntity(CSVRecord record);
}
