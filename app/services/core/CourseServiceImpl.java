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
    public CompletableFuture<Integer> getActiveCoursesByProfessorId(Long userId, String courseCode) {
        return courseRepository.findActiveCoursesByProfessorId(userId, courseCode);
    }

    @Override
    public CompletableFuture<List<Map<String, String>>> getAllCourses(Long userId) {
        return courseRepository.findAllCourses(userId);
    }
}
