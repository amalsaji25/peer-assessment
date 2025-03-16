package models;

import jakarta.persistence.*;
import repository.CourseRepository;
import repository.UserRepository;

import java.io.Serializable;

@Entity
@Table(name = "enrollments")
public class Enrollments implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Long enrollmentId;

    @ManyToOne
    @JoinColumn(name = "student_id", referencedColumnName = "user_id", nullable = false)
    private Users student;

    @ManyToOne
    @JoinColumn(name = "course_code", referencedColumnName = "course_code", nullable = false)
    private Courses course;

    public Enrollments(){}

    public Enrollments(String studentId, String courseCode, UserRepository userRepository, CourseRepository courseRepository) {
        this.student = userRepository.findById(studentId).orElseThrow(() -> new IllegalArgumentException("Student not found with id " + studentId));
        this.course = courseRepository.findByCourseCode(courseCode).orElseThrow(() -> new IllegalArgumentException("Course not found with id " + courseCode));
    }

    public Long getEnrollmentId() {
        return enrollmentId;
    }

    public Users getStudent() {
        return student;
    }

    public Courses getCourse() {
        return course;
    }
}
