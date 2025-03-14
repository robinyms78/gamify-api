package sg.edu.ntu.gamify_demo.services;
import org.springframework.stereotype.Component;

import sg.edu.ntu.gamify_demo.exceptions.UserValidationException;
import sg.edu.ntu.gamify_demo.models.User;

@Component
public class UserValidator {
    public void validateUser(User user) throws UserValidationException {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new UserValidationException("Username cannot be empty");
        }
        
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new UserValidationException("Email cannot be empty");
        }
        
        if (user.getPasswordHash() == null || user.getPasswordHash().trim().isEmpty()) {
            throw new UserValidationException("Password hash cannot be empty");
        }
        
        if (user.getRole() == null) {
            throw new UserValidationException("Role cannot be null");
        }
    }
}
