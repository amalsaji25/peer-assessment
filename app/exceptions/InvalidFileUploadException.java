package exceptions;

/**
 * Custom exception class for handling invalid file upload errors. This exception is thrown when a
 * file upload does not meet the expected format or contains invalid data.
 */
public class InvalidFileUploadException extends RuntimeException {

  /**
   * Constructs a new InvalidFileUploadException with the specified detail message.
   *
   * @param message the detail message
   */
  public InvalidFileUploadException(String message) {
    super(message);
  }
}
