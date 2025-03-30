package services.core;

import com.fasterxml.jackson.databind.node.ObjectNode;
import forms.AssignmentForm;
import models.*;
import models.dto.AssignmentEditDTO;
import models.dto.Context;
import models.enums.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Files;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import repository.core.AssignmentRepository;
import repository.core.CourseRepository;
import repository.core.FeedbackRepository;
import services.FileUploadService;
import services.processors.CSVProcessor;
import services.validations.AssignmentFormValidation;
import services.validations.ReviewTaskValidation;

import javax.inject.Inject;
import java.io.File;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.*;
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
    private final EnrollmentService enrollmentService;
    private final FeedbackRepository feedbackRepository;

    @Inject
    public AssignmentServiceImpl(AssignmentRepository assignmentRepository, AssignmentFormValidation assignmentFormValidation, CourseRepository courseRepository, FileUploadService fileUploadService, EnrollmentService enrollmentService, FeedbackRepository feedbackRepository) {
        this.assignmentRepository = assignmentRepository;
        this.assignmentFormValidation = assignmentFormValidation;
        this.courseRepository = courseRepository;
        this.fileUploadService = fileUploadService;
        this.enrollmentService = enrollmentService;
        this.feedbackRepository = feedbackRepository;
    }

    @Override
    public CompletableFuture<Integer> getAssignmentCountByProfessorId(Long userId, String courseCode, String courseSection, String term) {
        return assignmentRepository.findAssignmentCountByProfessorId(userId, courseCode, courseSection, term );
    }

    @Override
    public CompletableFuture<Result> createAssignment(AssignmentForm assignmentForm) {
        if(!assignmentFormValidation.isValid(assignmentForm)) {
            log.info("Invalid assignment creation form");
            return CompletableFuture.completedFuture(Results.badRequest("Invalid assignment creation form"));
        }

        Optional<Course> course = courseRepository.findByCourseCodeAndSectionAndTerm(assignmentForm.getCourseCode(), assignmentForm.getCourseSection(), assignmentForm.getTerm());

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
        assignment.setPeerAssigned(false);

        LocalDate today = LocalDate.now();
        boolean fileUploaded = course.get().isStudentFileUploaded();
        LocalDate startDate = assignmentForm.getStartDate();
        LocalDate dueDate = assignmentForm.getDueDate();

        if (!fileUploaded) {
            assignment.setStatus(Status.PENDING);
            assignment.setStatusReason("Student file not uploaded");
        } else if (today.isBefore(startDate)) {
            assignment.setStatus(Status.PENDING);
            assignment.setStatusReason("Peer assessment not yet started");
        } else if (today.isAfter(dueDate)) {
            assignment.setStatusReason("Peer assessment due date has passed");
            assignment.setStatus(Status.COMPLETED);
        } else {
            assignment.setStatusReason("Student file is uploaded and peer assessment start date is reached");
            assignment.setStatus(Status.ACTIVE);
        }


        List<FeedbackQuestion> feedbackQuestions = new ArrayList<>();
        assignmentForm.getQuestions().forEach(reviewQuestion -> {
            FeedbackQuestion feedbackQuestion = new FeedbackQuestion();
            feedbackQuestion.setQuestionText(reviewQuestion.question);
            feedbackQuestion.setMaxMarks(reviewQuestion.marks);
            feedbackQuestion.setAssignment(assignment);

            feedbackQuestions.add(feedbackQuestion);
        });

        // Private comment for professor
        FeedbackQuestion privateComment = new FeedbackQuestion();
        privateComment.setQuestionText("Private Comment for Professor");
        privateComment.setMaxMarks(0);
        privateComment.setAssignment(assignment);
        feedbackQuestions.add(privateComment);

        // Map the feedback questions to the assignment
        assignment.setFeedbackQuestions(feedbackQuestions);

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
        assignmentCreationForm.setCourse(getFirst(fields, "courseCode"));
        String course = getFirst(fields, "courseCode");
        String courseCode = course.split(":::")[0].trim();
        String courseSection = course.split(":::")[1].trim();
        String term = course.split(":::")[2].trim();
        assignmentCreationForm.setCourseCode(courseCode);
        assignmentCreationForm.setCourseSection(courseSection);
        assignmentCreationForm.setTerm(term);
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
          Course course =
              courseRepository
                  .findByCourseCodeAndSectionAndTerm(form.courseCode, form.courseSection, form.getTerm())
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

            // Handle deletions
            if (!existingFeedbackQuestions.isEmpty()) {
                // 1. Collect IDs of removed questions
                Set<Long> removedQuestionIds = existingFeedbackQuestions.values().stream()
                        .map(FeedbackQuestion::getQuestionId)
                        .collect(Collectors.toSet());

                // 2. Remove all feedbacks from review tasks that reference those questions
                for (ReviewTask task : assignment.getReviewTasks()) {
                    List<Feedback> updatedFeedbacks = new ArrayList<>();

                    for (Feedback fb : task.getFeedbacks()) {
                        if (fb.getQuestion() != null && removedQuestionIds.contains(fb.getQuestion().getQuestionId())) {
                            feedbackRepository.deleteById(fb.getId());
                        } else {
                            updatedFeedbacks.add(fb);
                        }
                    }

                    task.setFeedbacks(updatedFeedbacks);
                }

                // 3. Remove the questions themselves from assignment
                for (FeedbackQuestion q : existingFeedbackQuestions.values()) {
                    assignment.getFeedbackQuestions().remove(q); // Orphan removal if enabled
                }
            }

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
        assignmentEditDTO.setCourseSection(assignment.getCourse().getCourseSection());
        assignmentEditDTO.setTerm(assignment.getCourse().getTerm());
        return assignmentEditDTO;
    }

    @Override
    public CompletableFuture<List<Map<String,Object>>> fetchAssignmentsForCourse(String course) {
        String courseCode = course.split(":::")[0].trim();
        String courseSection = course.split(":::")[1].trim();
        String term = course.split(":::")[2].trim();
        return assignmentRepository.findAssignmentsByCourse(courseCode, courseSection, term );
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

    @Override
    public CompletableFuture<Integer> getAssignmentCountByStudentId(Long userId, String courseCode) {
        return enrollmentService.findStudentEnrolledCourseCodes(userId, courseCode)
                .thenCompose(courseCodes -> {
                    if (courseCodes.isEmpty()) return CompletableFuture.completedFuture(0);
                    return assignmentRepository.findAssignmentCountByCourseCodes(courseCodes);
                });
    }


}
