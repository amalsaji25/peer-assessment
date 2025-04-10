package services.core;

import repository.core.EnrollmentRepository;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class EnrollmentServiceImpl implements EnrollmentService {

    private EnrollmentRepository enrollmentRepository;

    @Inject
    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    public CompletableFuture<Integer> getStudentCountByProfessorId(Long userId, String courseCode, String courseSection, String term) {
        return enrollmentRepository.getStudentCountByProfessorId(userId,courseCode, courseSection, term );
    }

    @Override
    public CompletableFuture<List<String>> findStudentEnrolledCourseCodes(Long userId, String courseCode) {
        if (courseCode == null || courseCode.equalsIgnoreCase("all")) {
            return enrollmentRepository.findCourseCodesByStudentId(userId);
        } else {
            return enrollmentRepository.isStudentEnrolledInCourse(userId, courseCode)
                    .thenApply(isEnrolled -> isEnrolled ? List.of(courseCode) : List.of());
        }
    }

    @Override
    public CompletableFuture<List<Map<String,String>>> getStudentEnrolledCourse(Long userId) {
        return enrollmentRepository.findEnrolledCoursesForStudentId(userId);
    }

}
