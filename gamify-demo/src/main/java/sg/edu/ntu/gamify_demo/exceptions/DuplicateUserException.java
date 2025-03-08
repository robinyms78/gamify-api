package sg.edu.ntu.gamify_demo.exceptions;

/**
 * Exception thrown when attempting to register a user with a username or email
 * that already exists in the system.
 */
public class DuplicateUserException extends RuntimeException {
    public DuplicateUserException(String message) {
        super(message);
    }
}
