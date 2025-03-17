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

    public Enrollments(Users student, Courses course) {
        this.student = student;
        this.course = course;
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
