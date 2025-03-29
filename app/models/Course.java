package models;


import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "courses")
public class Course implements Serializable {

    @Id
    @Column(name="course_code", unique = true, nullable = false, length = 100)
    private String courseCode;

    @Column(name = "course_name", nullable = false, length = 100)
    private String courseName;

    @ManyToOne
    @JoinColumn(name = "professor_id", referencedColumnName = "user_id", nullable = false)
    private User professor;

    @OneToMany(mappedBy = "course", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Enrollment> enrollments;

    public Course(){}

    public Course(String courseCode, String courseName, User professor) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.professor = professor;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseName(){
        return courseName;
    }

    public User getProfessor(){
        return professor;
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }
}
