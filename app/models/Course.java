package models;


import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "courses",
        uniqueConstraints = @UniqueConstraint(columnNames = {"course_code", "course_section", "term"}),
        indexes = @Index(name = "course", columnList = "course_code,course_section, term"))
public class Course implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long courseId;

    @Column(name="course_code", nullable = false, length = 100)
    private String courseCode;

    @Column(name = "course_name", nullable = false, length = 100)
    private String courseName;

    @Column(name = "course_section", nullable = false, length = 100)
    private String courseSection;

    @ManyToOne
    @JoinColumn(name = "professor_id", referencedColumnName = "user_id", nullable = false)
    private User professor;

    @OneToMany(mappedBy = "course", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Enrollment> enrollments;

    @Column(name = "term", nullable = false)
    private String term;

    @Column(name = "is_student_file_uploaded", nullable = false)
    private boolean isStudentFileUploaded = false;

    public Course(){}

    public Course(String courseCode, String courseName, User professor, String term, String courseSection, Boolean isStudentFileUploaded) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.professor = professor;
        this.term = term;
        this.courseSection = courseSection;
        this.isStudentFileUploaded = isStudentFileUploaded;
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

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public boolean isStudentFileUploaded() {
        return isStudentFileUploaded;
    }

    public void setStudentFileUploaded(boolean studentFileUploaded) {
        isStudentFileUploaded = studentFileUploaded;
    }

    public String getCourseSection() {
        return courseSection;
    }

    public void setCourseSection(String courseSection) {
        this.courseSection = courseSection;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
}
