package exceptions;

/**
 * Custom exception class for handling invalid CSV file errors.
 * This exception is thrown when a CSV file does not meet the expected format or contains invalid data.
 */
public class InvalidCsvException extends RuntimeException {

    /**
     * Constructs a new InvalidCsvException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidCsvException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidCsvException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public InvalidCsvException(String message, Throwable cause) {
        super(message, cause);
    }
}
