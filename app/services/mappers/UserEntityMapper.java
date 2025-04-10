package services.mappers;

import models.User;
import models.dto.Context;
import services.processors.record.InputRecord;

import javax.inject.Singleton;


@Singleton
public class UserEntityMapper implements EntityMapper<User>{
    @Override
    public User mapToEntity(InputRecord record, Context context) {
        return new User(
                Long.parseLong(record.get("user_id")),
                record.get("email"),
                "",
                record.get("first_name"),
                record.get("last_name"),
                record.get("role")
        );
    }
}
