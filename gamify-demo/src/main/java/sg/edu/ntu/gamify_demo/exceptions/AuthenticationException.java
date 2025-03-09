package sg.edu.ntu.gamify_demo.exceptions;

/**
 * Exception thrown when authentication fails.
 * This could be due to invalid credentials, expired tokens, etc.
 */
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}
