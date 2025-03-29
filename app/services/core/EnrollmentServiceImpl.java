package services.core;

import repository.core.EnrollmentRepository;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

public class EnrollmentServiceImpl implements EnrollmentService {

    private EnrollmentRepository enrollmentRepository;

    @Inject
    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    public CompletableFuture<Integer> getStudentCountByProfessorId(Long userId, String courseCode) {
        return enrollmentRepository.getStudentCountByProfessorId(userId,courseCode);
    }

}
