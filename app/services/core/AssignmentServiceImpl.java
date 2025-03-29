package services.core;

import com.fasterxml.jackson.databind.node.ObjectNode;
import forms.AssignmentForm;
import models.*;
import models.dto.AssignmentEditDTO;
import models.dto.AssignmentExportDTO;
import models.dto.AssignmentUploadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Files;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import repository.core.AssignmentRepository;
import repository.core.CourseRepository;
import services.FileUploadService;
import services.processors.CSVProcessor;
import services.validations.AssignmentFormValidation;
import services.validations.ReviewTaskValidation;
import services.validations.Validations;

import javax.inject.Inject;
import java.io.File;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentFormValidation assignmentFormValidation;
    private final CourseRepository courseRepository;
    private final FileUploadService fileUploadService;
    private static final Logger log = LoggerFactory.getLogger(AssignmentServiceImpl.class);

    @Inject
    public AssignmentServiceImpl(AssignmentRepository assignmentRepository, AssignmentFormValidation assignmentFormValidation, CourseRepository courseRepository, FileUploadService fileUploadService) {
        this.assignmentRepository = assignmentRepository;
        this.assignmentFormValidation = assignmentFormValidation;
        this.courseRepository = courseRepository;
        this.fileUploadService = fileUploadService;
    }

    @Override
    public CompletableFuture<Integer> getAssignmentCountByProfessorId(Long userId, String courseCode) {
        return assignmentRepository.findAssignmentCountByProfessorId(userId, courseCode);
    }

    @Override
    public CompletableFuture<Result> createAssignment(AssignmentForm assignmentForm, List<ReviewTask> reviewTasks) {
        if(!assignmentFormValidation.isValid(assignmentForm)) {
            log.info("Invalid assignment creation form");
            return CompletableFuture.completedFuture(Results.badRequest("Invalid assignment creation form"));
        }

        Optional<Course> course = courseRepository.findByCourseCode(assignmentForm.getCourseCode());

        if(course.isEmpty()) {
            log.info("Course not found");
            return CompletableFuture.completedFuture(Results.badRequest("Course not found"));
        }

        Assignment assignment = new Assignment();
        assignment.setCourse(course.get());
        assignment.setTitle(assignmentForm.getTitle());
        assignment.setDescription(assignmentForm.getDescription());
        assignment.setStartDate(assignmentForm.getStartDate());
        assignment.setDueDate(assignmentForm.getDueDate());


        List<FeedbackQuestion> feedbackQuestions = new ArrayList<>();
        assignmentForm.getQuestions().forEach(reviewQuestion -> {
            FeedbackQuestion feedbackQuestion = new FeedbackQuestion();
            feedbackQuestion.setQuestionText(reviewQuestion.question);
            feedbackQuestion.setMaxMarks(reviewQuestion.marks);
            feedbackQuestion.setAssignment(assignment);

            feedbackQuestions.add(feedbackQuestion);
        });

        // Map the feedback questions to the assignment
        assignment.setFeedbackQuestions(feedbackQuestions);

        // Map the review tasks to the assignment
        assignment.setReviewTasks(reviewTasks);

        // Map the assignment to each task
        reviewTasks.forEach(reviewTask -> {reviewTask.setAssignment(assignment);});

        reviewTasks.forEach(reviewTask -> {

            List<Feedback> reviewTaskFeedbacks = new ArrayList<>();
            assignment.getFeedbackQuestions().forEach(feedbackQuestion -> {
                Feedback feedback = new Feedback();
                feedback.setQuestion(feedbackQuestion);
                feedback.setReviewTask(reviewTask);
                feedback.setScore(0); //default
                feedback.setFeedbackText(""); //default
                reviewTaskFeedbacks.add(feedback);
            });
            reviewTask.setFeedbacks(reviewTaskFeedbacks);
        });

        assignmentRepository.save(assignment);

        log.info("Assignment created successfully with ID: {}", assignment.getAssignmentId());


        ObjectNode successJson = Json.newObject();
        successJson.put("message", "Assignment created successfully");
        successJson.put("assignmentId", assignment.getAssignmentId());

        return CompletableFuture.completedFuture(Results.ok(successJson));
    }

    @Override
    public CompletableFuture<AssignmentForm> parseAssignmentForm(Http.MultipartFormData assignmentForm) {
        try{
        AssignmentForm assignmentCreationForm = new AssignmentForm();
        Map<String,String[]> fields = assignmentForm.asFormUrlEncoded();
        assignmentCreationForm.setTitle(getFirst(fields, "title"));
        assignmentCreationForm.setCourseCode(getFirst(fields, "courseCode"));
        assignmentCreationForm.setDescription(getFirst(fields, "description"));
        String startDate = getFirst(fields,"startDate");
        String dueDate = getFirst(fields,"dueDate");

        if (startDate != null) assignmentCreationForm.setStartDate(LocalDate.parse(startDate));
        if (dueDate != null) assignmentCreationForm.setDueDate(LocalDate.parse(dueDate));

        String[] questionTexts = fields.get("reviewQuestions[]");
        String[] questionMarks = fields.get("questionMarks[]");
        String[] questionIds = fields.get("questionIds[]");

        if (questionTexts != null && questionMarks != null && questionTexts.length == questionMarks.length) {
            for (int i = 0; i < questionTexts.length; i++) {

                Long questionId = (questionIds != null && i < questionIds.length && !questionIds[i].isBlank()) ? Long.parseLong(questionIds[i]) : null;
                String question = questionTexts[i];
                int marks = Integer.parseInt(questionMarks[i]);

                assignmentCreationForm.getQuestions().add(new AssignmentForm.ReviewQuestionForm(questionId, question, marks));
            }
        }
        log.info("Parsed assignment creation form data with title {}", assignmentCreationForm.getTitle());
        return CompletableFuture.completedFuture(assignmentCreationForm);
        } catch (Exception e) {
            log.error("Error in parsing assignment creation form data with error {}", e.getMessage());
            return CompletableFuture.completedFuture(null);
        }
    }

    private String getFirst(Map<String, String[]> map, String key) {
        String[] values = map.get(key);
        return (values != null && values.length > 0) ? values[0] : null;
    }

    @Override
    public CompletableFuture<List<ReviewTask>> parseAssignmentTaskTeamInfo(AssignmentUploadContext context) {
        Http.MultipartFormData<Files.TemporaryFile> body = context.getBody();
        Optional<Http.MultipartFormData.FilePart<Files.TemporaryFile>> filePart = Optional.ofNullable(body.getFile("file"));

        if(filePart.isEmpty()){
            return CompletableFuture.completedFuture(null);
        }

        File uploadDir = new File("uploads");
        if(!uploadDir.exists()){
            uploadDir.mkdirs();
        }

        Http.MultipartFormData.FilePart<Files.TemporaryFile> file = filePart.get();

        Files.TemporaryFile temporaryFile = file.getRef();
        File uploadedFile = new File(uploadDir, file.getFilename());
        temporaryFile.copyTo(uploadedFile.toPath(),true);

        return fileUploadService.getFileProcessor(uploadedFile,"review_tasks")
                .thenCompose(fileProcessor -> {
                    if(fileProcessor instanceof CSVProcessor<?> csvProcessor) {
                        try{
                            Field validationField = CSVProcessor.class.getDeclaredField("validations");
                            validationField.setAccessible(true);
                            Object validations = validationField.get(csvProcessor);

                            if(validations instanceof ReviewTaskValidation reviewTaskValidations) {
                                reviewTaskValidations.setCourseCode(context.getCourseCode());
                            }
                        }catch (NoSuchFieldException | IllegalAccessException e){
                            log.error("Failed to inject courseCode into ReviewTaskValidations", e);
                        }
                    }
                    return fileUploadService.parseAndProcessFile(fileProcessor, uploadedFile);
                })
                .thenApply(processedData ->
                        processedData.stream()
                        .map(record -> (ReviewTask) record)
                        .toList());
    }

    @Override
    public CompletableFuture<AssignmentEditDTO> getAssignmentDetails(Long assignmentId) {
        return CompletableFuture.supplyAsync(() -> {
            Assignment assignment = assignmentRepository.findByIdWithFeedbackQuestions(assignmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

            AssignmentEditDTO assignmentEditDTO = setAssignmentEditDTO(assignment);

            assignmentEditDTO.reviewQuestions = assignment.getFeedbackQuestions().stream()
                    .map(feedbackQuestion -> {
                        AssignmentEditDTO.ReviewQuestionDTO reviewQuestionDTO = new AssignmentEditDTO.ReviewQuestionDTO();
                        reviewQuestionDTO.questionId = feedbackQuestion.getQuestionId();
                        reviewQuestionDTO.question = feedbackQuestion.getQuestionText();
                        reviewQuestionDTO.marks = feedbackQuestion.getMaxMarks();
                        return reviewQuestionDTO;
                    })
                    .toList();
            return assignmentEditDTO;
            });
    }

    @Override
    public CompletionStage<Void> updateAssignment(Long assignmentId, AssignmentForm form) {
        return CompletableFuture.runAsync(() -> {

            // Fetch the assignment and the existing feedback questions
            Assignment assignment = assignmentRepository.findByIdWithFeedbackQuestions(assignmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

            // Update the assignment details from the latest form
            assignment.setTitle(form.title);
            assignment.setDescription(form.description);
            assignment.setStartDate(form.startDate);
            assignment.setDueDate(form.dueDate);

            // Update the course details
            Course course = courseRepository.findByCourseCode(form.courseCode)
                    .orElseThrow(() -> new IllegalArgumentException("Course not found"));
            assignment.setCourse(course);

            // Map exiting feedback questions by id

            Map<Long, FeedbackQuestion> existingFeedbackQuestions = assignment.getFeedbackQuestions().stream()
                    .filter(feedbackQuestion -> feedbackQuestion.getQuestionId() != null)
                    .collect(Collectors.toMap(FeedbackQuestion::getQuestionId, Function.identity()));

            // Add new feedback questions

            List<FeedbackQuestion> updatedFeedbackQuestions = new ArrayList<>();
            form.getQuestions().forEach(reviewQuestion -> {
                FeedbackQuestion feedbackQuestion;
                if(reviewQuestion.questionId != null && existingFeedbackQuestions.containsKey(reviewQuestion.questionId)) {
                    //Update existing feedback question
                    feedbackQuestion = existingFeedbackQuestions.get(reviewQuestion.questionId);
                    feedbackQuestion.setQuestionText(reviewQuestion.question);
                    feedbackQuestion.setMaxMarks(reviewQuestion.marks);
                    existingFeedbackQuestions.remove(reviewQuestion.questionId);
                }
                else{
                    // Add new feedback question
                    feedbackQuestion = new FeedbackQuestion();
                    feedbackQuestion.setQuestionText(reviewQuestion.question);
                    feedbackQuestion.setMaxMarks(reviewQuestion.marks);
                    feedbackQuestion.setAssignment(assignment);
                }
                updatedFeedbackQuestions.add(feedbackQuestion);
            });

            // Map the feedback questions to the assignment
            assignment.setFeedbackQuestions(updatedFeedbackQuestions);

            // Create default feedback entries for new questions
            List<FeedbackQuestion> newQuestions = updatedFeedbackQuestions.stream()
                    .filter(q -> q.getQuestionId() == null)
                    .toList();

            if (!newQuestions.isEmpty()) {
                for (ReviewTask task : assignment.getReviewTasks()) {
                    List<Feedback> feedbacks = task.getFeedbacks();
                    if (feedbacks == null) {
                        feedbacks = new ArrayList<>();
                    }

                    for (FeedbackQuestion newQ : newQuestions) {
                        Feedback feedback = new Feedback();
                        feedback.setQuestion(newQ);
                        feedback.setReviewTask(task);
                        feedback.setScore(0);
                        feedback.setFeedbackText("");
                        feedbacks.add(feedback);
                    }

                    task.setFeedbacks(feedbacks);
                }
            }

            assignmentRepository.update(assignment); // merge
        });
    }


    private static AssignmentEditDTO setAssignmentEditDTO(Assignment assignment) {
        AssignmentEditDTO assignmentEditDTO = new AssignmentEditDTO();
        assignmentEditDTO.setAssignmentId(assignment.getAssignmentId());
        assignmentEditDTO.setTitle(assignment.getTitle());
        assignmentEditDTO.setDescription(assignment.getDescription());
        assignmentEditDTO.setStartDate(assignment.getStartDate().toString());
        assignmentEditDTO.setDueDate(assignment.getDueDate().toString());
        assignmentEditDTO.setCourseCode(assignment.getCourse().getCourseCode());
        return assignmentEditDTO;
    }

    @Override
    public CompletableFuture<List<Map<String,Object>>> fetchAssignmentsForCourse(String courseCode) {
        return assignmentRepository.findAssignmentsByCourseId(courseCode);
    }

  @Override
  public CompletableFuture<Boolean> deleteAssignment(Long assignmentId) {
    Optional<Assignment> assignmentOptional = assignmentRepository.findById(assignmentId);
    if (assignmentOptional.isEmpty()) {
      log.error("Assignment with ID {} not found", assignmentId);
      return CompletableFuture.completedFuture(false);
    }

    // Step 4: Delete assignment itself
    return assignmentRepository.deleteAssignmentById(assignmentId);
    }


}
