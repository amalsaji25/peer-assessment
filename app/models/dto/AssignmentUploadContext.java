package models.dto;

import play.libs.Files;
import play.mvc.Http;

public class AssignmentUploadContext {
    private String courseCode;
    private Http.MultipartFormData<Files.TemporaryFile> body;

    public AssignmentUploadContext() {}

    public AssignmentUploadContext(String courseCode, Http.MultipartFormData<Files.TemporaryFile> body) {
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
}
