package services.mappers;

import models.Course;
import models.User;
import models.dto.Context;
import repository.core.UserRepository;
import services.processors.record.InputRecord;

import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public class CourseEntityMapper implements EntityMapper<Course> {

    private final UserRepository userRepository;

    @Inject
    public CourseEntityMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Course mapToEntity(InputRecord record, Context context) {
        String courseCode = record.get("course_code").trim();
        String courseName = record.get("course_name").trim();
        Long professorId = Long.valueOf(record.get("professor_id").trim());
        String courseSection = record.get("course_section").trim();
        Boolean isStudentFileUploaded = false;

        User professor = userRepository.findById(professorId).orElse(null);
        String term = record.get("term").trim();

        return new Course(courseCode, courseName, professor, term, courseSection, isStudentFileUploaded);
    }
}
