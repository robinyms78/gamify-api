package sg.edu.ntu.gamify_demo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an achievement is not found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class AchievementNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public AchievementNotFoundException(String achievementId) {
        super("Achievement not found with ID: " + achievementId);
    }
    
    public AchievementNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
