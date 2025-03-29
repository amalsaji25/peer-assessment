package services.validations;

import models.User;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.core.Repository;
import repository.core.UserRepository;

import javax.inject.Singleton;
import java.util.List;
import java.util.regex.Pattern;

@Singleton
public class UserValidation implements Validations<User>{

    private static final Logger log = LoggerFactory.getLogger(UserValidation.class);
    private static final List<String> mandatoryFields = List.of("user_id", "email", "password", "first_name", "last_name","role");
    private static final List<String> expectedFieldsOrder = List.of("user_id", "email", "password", "first_name", "last_name", "role");
    private static final List<String> ALLOWED_ROLES = List.of("student", "professor");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @Override
    public boolean validateSyntax(CSVRecord record) {
        boolean isValidSyntax = mandatoryFields.stream().allMatch(field -> record.isMapped(field) && !record.get(field).isEmpty());

        if(!isValidSyntax) {
            log.warn("Skipping invalid user record due to missing mandatory fields :{}", record);
        }
        return isValidSyntax;
    }

    @Override
    public boolean validateSemantics(User record, Repository<User> repository) {
        UserRepository userRepository = (UserRepository) repository;

        if(userRepository.findById(record.getUserId()).isPresent()) {
            log.warn("User {} already exists", record.getUserId());
            return false;
        }

        if (!EMAIL_PATTERN.matcher(record.getEmail()).matches()) {
            log.warn("Invalid email format for user: {} with email given as : {}", record.getUserId(), record.getEmail());
            return false;
        }

        if (!ALLOWED_ROLES.contains(record.getRole().toLowerCase())) {
            log.warn("Invalid user role for user ID: {} - Role: {}", record.getUserId(), record.getRole());
            return false;
        }

        return true;
    }


    @Override
    public boolean validateFieldOrder(List<String> actualHeaders) {
        boolean isCorrectOrder = actualHeaders.equals(expectedFieldsOrder);
        if(!isCorrectOrder) {
            log.warn("File has incorrect field order. Expected order is {}, Found order is {}", expectedFieldsOrder, actualHeaders);
        }
        return isCorrectOrder;
    }
}
