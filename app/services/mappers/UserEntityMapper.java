package services.mappers;

import javax.inject.Singleton;
import models.User;
import models.dto.Context;
import services.processors.record.InputRecord;

/**
 * UserEntityMapper is a service class that implements the EntityMapper interface. It provides a
 * method to map input records to User entities.
 */
@Singleton
public class UserEntityMapper implements EntityMapper<User> {

  /**
   * Maps an input record to a User entity. It retrieves the user ID, email, first name, last name,
   * and role from the input record and creates a new User entity with the retrieved values.
   *
   * @param record the input record containing user information
   * @param context the context in which the mapping is performed
   * @return a User entity mapped from the input record
   */
  @Override
  public User mapToEntity(InputRecord record, Context context) {
    return new User(
        Long.parseLong(record.get("user_id")),
        record.get("email"),
        "",
        record.get("first_name"),
        record.get("last_name"),
        record.get("role"));
  }
}
