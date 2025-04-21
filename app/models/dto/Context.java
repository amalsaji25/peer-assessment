package models.dto;

import play.libs.Files;
import play.mvc.Http;

/**
 * Context is a data transfer object (DTO) that represents the context of a file upload operation.
 * It contains fields for the course code, course section, term, user ID, and the uploaded file
 * data.
 */
public class Context {
  private String courseCode;
  private String courseSection;
  private String term;
  private String userId;
  private Http.MultipartFormData<Files.TemporaryFile> body;

  public Context() {}

  public Context(String courseCode, Http.MultipartFormData<Files.TemporaryFile> body) {
    this.courseCode = courseCode;
    this.body = body;
  }

  public String getCourseCode() {
    return courseCode;
  }

  public void setCourseCode(String courseCode) {
    this.courseCode = courseCode;
  }

  public Http.MultipartFormData<Files.TemporaryFile> getBody() {
    return body;
  }

  public void setBody(Http.MultipartFormData<Files.TemporaryFile> body) {
    this.body = body;
  }

  public String getCourseSection() {
    return courseSection;
  }

  public void setCourseSection(String courseSection) {
    this.courseSection = courseSection;
  }

  public String getTerm() {
    return term;
  }

  public void setTerm(String term) {
    this.term = term;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }
}
