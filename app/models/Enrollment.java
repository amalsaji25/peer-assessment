package models;

import jakarta.persistence.*;
import java.io.Serializable;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Enrollment is an entity class that represents a student's enrollment in a course. It contains
 * fields for the enrollment's ID, student, course, course section, and term. The class also
 * includes methods for getting and setting these fields.
 */
@Entity
@Table(
    name = "enrollments",
    uniqueConstraints =
        @UniqueConstraint(columnNames = {"student_id", "course_code", "course_section", "term"}),
    indexes =
        @Index(name = "enrollment", columnList = "student_id,course_code, course_section,term"))
public class Enrollment implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "enrollment_id")
  private Long enrollmentId;

  @ManyToOne
  @JoinColumn(name = "student_id", referencedColumnName = "user_id", nullable = false)
  private User student;

  @ManyToOne
  @JoinColumn(name = "course_code", referencedColumnName = "course_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Course course;

  @Column(name = "course_section", nullable = false)
  private String courseSection;

  @Column(name = "term", nullable = false)
  private String term;

  public Enrollment() {}

  public Enrollment(User student, Course course, String courseSection, String term) {
    this.student = student;
    this.course = course;
    this.courseSection = courseSection;
    this.term = term;
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

  public String getTerm() {
    return term;
  }

  public String getCourseSection() {
    return courseSection;
  }

  public void setCourseSection(String courseSection) {
    this.courseSection = courseSection;
  }
}
