package services.core;

import repository.core.CourseRepository;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    @Inject
    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public CompletableFuture<Integer> getActiveCoursesByProfessorId(Long userId, String courseCode, String courseSection, String term) {
        return courseRepository.findActiveCoursesByProfessorId(userId, courseCode, courseSection, term );
    }

    @Override
    public CompletableFuture<List<Map<String, String>>> getAllCourses(Long userId, String term) {
        return courseRepository.findAllCourses(userId, term);
    }

    @Override
    public CompletableFuture<List<String>> getAllTerms(Long userId) {
        return courseRepository.findAllTerms(userId);
    }
}
