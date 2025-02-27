package in.siddharthsabron.clik.exceptions;

/**
 * Custom exception to indicate that a URL has already been shortened.
 */
public class DuplicateUrlException extends RuntimeException {
    public DuplicateUrlException(String message) {
        super(message);
    }

    public DuplicateUrlException(String message, Throwable cause) {
        super(message, cause);
    }
}