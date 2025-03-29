package services.validations;

import models.ReviewTask;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.core.Repository;
import repository.core.ReviewTaskRepository;

import java.util.List;

public class ReviewTaskValidation implements Validations<ReviewTask> {

    private static final List<String> mandatoryFields = List.of("Group ID", "Group Name", "Group Size");
    private final static Logger log = LoggerFactory.getLogger(ReviewTaskValidation.class);
    private String courseCode;

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    @Override
    public boolean validateSyntax(CSVRecord record) {
        boolean isValidSyntax = mandatoryFields.stream().allMatch(field -> record.isMapped(field) && !record.get(field).isEmpty());

        if(!isValidSyntax) {
            log.warn("Missing one of the mandatory fields :{}", record);
            return false;
        }
        int groupSize;
        try{
            groupSize = Integer.parseInt(record.get("Group Size"));
        }catch(NumberFormatException e){
            log.warn("Group Size is not a valid number :{}", record);
            return false;
        }

        for(int i=1; i <= groupSize; i++){
            String memberIdField = "Member " + i + " ID Number";
            if(!record.isMapped(memberIdField) || record.get(memberIdField).isEmpty()){
                log.warn("Missing ID Number for member {} in group :{}", i, record);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean validateSemantics(ReviewTask reviewTask, Repository<ReviewTask> repository) {

        ReviewTaskRepository reviewTaskRepository = (ReviewTaskRepository) repository;
        if (reviewTask.getReviewer() == null || reviewTask.getReviewee() == null) {
                log.warn("Skipping task with null reviewer or reviewee. GroupId={}, GroupName={}", reviewTask.getGroupId(), reviewTask.getGroupName());
                return false;
        }

        if (reviewTask.getReviewer().getUserId().equals(reviewTask.getReviewee().getUserId())) {
            log.warn("Reviewer and reviewee cannot be the same. GroupId={}, GroupName={}", reviewTask.getGroupId(), reviewTask.getGroupName());
            return false;
        }

        if (reviewTask.getGroupSize() <= 0) {
            log.warn("Group size must be greater than 0. GroupId={}, GroupName={}", reviewTask.getGroupId(), reviewTask.getGroupName());
            return false;
        }

        if (reviewTask.getGroupName() == null || reviewTask.getGroupName().isEmpty()) {
            log.warn("Group name cannot be null or empty. GroupId={}, GroupName={}", reviewTask.getGroupId(), reviewTask.getGroupName());
            return false;
        }

        if (reviewTask.getGroupId() == null || reviewTask.getGroupId() <= 0) {
            log.warn("Group ID must be greater than 0. GroupId={}, GroupName={}", reviewTask.getGroupId(), reviewTask.getGroupName());
            return false;
        }

        if(!reviewTaskRepository.validateIfReviewerAndRevieweeIsEnrolledInCourse(reviewTask.getReviewer().getUserId(), reviewTask.getReviewee().getUserId(),courseCode)){
            log.warn("Reviewer or reviewee is not enrolled in the course. ReviewerId={}, RevieweeId={}, CourseCode={}", reviewTask.getReviewer().getUserId(), reviewTask.getReviewee().getUserId(), courseCode);
            return false;
        }
        return true;
    }

    @Override
    public boolean validateFieldOrder(List<String> actualHeaders) {
        List<String> expectedStaticHeaders = List.of("Group ID", "Group Name", "Group Size", "Group Description",
                "Assigned teacher Username", "Assigned teacher Firstname",
                "Assigned teacher Lastname", "Assigned teacher Email");

        if(actualHeaders.size() < expectedStaticHeaders.size()){
            log.warn("File has header count less than expected mandatory header count. Expected header order is {}, Found order is {}", expectedStaticHeaders, actualHeaders);
            return false;
        }

        for(int i=0; i< expectedStaticHeaders.size(); i++){
            if(!expectedStaticHeaders.get(i).equals(actualHeaders.get(i))){
                log.warn("File has incorrect field order. Expected order is {}, Found order is {}", expectedStaticHeaders, actualHeaders);
                return false;
            }
        }

        int memberFields = actualHeaders.size() - expectedStaticHeaders.size();
        if(memberFields % 5 != 0){
            log.warn("File has incorrect fields for members. Expected fields for each member is 5, containing Username, ID Number, Firstname, Lastname, Email");
            return false;
        }

        String memberPrefix = "Member ";
        String[] memberSuffix = {"Username", "ID Number", "Firstname", "Lastname", "Email"};

        for(int i=0; i< memberFields; i++){
            int index = expectedStaticHeaders.size() + i;
            int memberIndex = i / 5 + 1;
            String expectedMemberHeader = memberPrefix + memberIndex + " " + memberSuffix[i % 5];

            if(!actualHeaders.get(index).equalsIgnoreCase(expectedMemberHeader)){
                log.warn("Team member header mismatch at index {}. Expected header is {}, Found header is {}", index, expectedMemberHeader, actualHeaders.get(index));
                return false;
            }
        }
        return true;
    }

}
