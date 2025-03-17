package services.mappers;

import org.apache.commons.csv.CSVRecord;

import javax.inject.Singleton;


public interface EntityMapper <T>{
    T mapToEntity(CSVRecord record);
}
