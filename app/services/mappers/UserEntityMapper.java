package services.mappers;

import models.Users;
import org.apache.commons.csv.CSVRecord;

import javax.inject.Singleton;


@Singleton
public class UserEntityMapper implements EntityMapper<Users>{
    @Override
    public Users mapToEntity(CSVRecord record) {
        return new Users(
                record.get("user_id"),
                record.get("email"),
                record.get("password"),
                record.get("first_name"),
                record.isMapped("middle_name") ? record.get("middle_name") : "",
                record.get("last_name"),
                record.get("role")
        );
    }
}
