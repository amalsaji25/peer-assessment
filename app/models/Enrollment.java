package models;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "enrollments",uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_code"}))
public class Enrollment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Long enrollmentId;

    @ManyToOne
    @JoinColumn(name = "student_id", referencedColumnName = "user_id", nullable = false)
    private User student;

    @ManyToOne
    @JoinColumn(name = "course_code", referencedColumnName = "course_code", nullable = false)
    private Course course;

    public Enrollment(){}

    public Enrollment(User student, Course course) {
        this.student = student;
        this.course = course;
    }

    public Long getEnrollmentId() {
        return enrollmentId;
    }

    public User getStudent() {
        return student;
    }

    public Course getCourse() {
        return course;
    }
}
