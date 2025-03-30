package services.mappers;

import models.ReviewTask;
import models.User;
import models.enums.Status;
import org.apache.commons.csv.CSVRecord;
import repository.core.UserRepository;
import services.processors.record.InputRecord;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class ReviewTaskEntityMapper implements EntityMapper<ReviewTask> {

    private final UserRepository userRepository;

    @Inject
    public ReviewTaskEntityMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<ReviewTask> mapToEntityList(InputRecord record) {
        Long groupId = Long.parseLong(record.get("Group ID").trim());
        String groupName = record.get("Group Name").trim();
        int groupSize = Integer.parseInt(record.get("Group Size").trim());

        List<User> users = new ArrayList<>();
        for(int i = 1; i <= groupSize; i++) {
            String userIdField = "Member " + i + " ID Number";
            Long userId = Long.parseLong(record.get(userIdField).trim());
            User user = userRepository.findById(userId).orElse(null);
            users.add(user);
        }

        List<ReviewTask> reviewTasks = new ArrayList<>();
        for(User reviewer : users) {
            for(User reviewee : users)
                if(reviewer != null && reviewee != null && !reviewer.getUserId().equals(reviewee.getUserId())){
                    ReviewTask reviewTask = new ReviewTask(
                            null,
                            reviewer,
                            reviewee,
                            Status.PENDING,
                            groupId,
                            groupName,
                            groupSize
                    );
                    reviewTasks.add(reviewTask);
                }
        }
        return reviewTasks;
    }

    @Override
    public ReviewTask mapToEntity(InputRecord record) {
        return null;
    }
}
