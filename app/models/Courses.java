package models;


import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "courses")
public class Courses implements Serializable {

    @Id
    @Column(name="course_code", unique = true, nullable = false, length = 100)
    private String courseCode;

    @Column(name = "course_name", nullable = false, length = 100)
    private String courseName;

    @ManyToOne
    @JoinColumn(name = "professor_id", referencedColumnName = "user_id", nullable = false)
    private Users professor;

    @OneToMany(mappedBy = "course", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Enrollments> enrollments;

    public Courses(){}

    public Courses(String courseCode, String courseName, Users professor) {
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

    public Users getProfessor(){
        return professor;
    }

    public List<Enrollments> getEnrollments() {
        return enrollments;
    }
}
