package exceptions;

public class InvalidFileUploadException extends RuntimeException {

  public InvalidFileUploadException(String message) {
    super(message);
  }

    public InvalidFileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
