package sg.edu.ntu.gamify_demo.dtos;

import jakarta.validation.constraints.*;
import sg.edu.ntu.gamify_demo.models.enums.UserRole;

/**
 * Data Transfer Object for registration requests.
 * Contains all necessary information to register a new user.
 */
public record RegistrationRequest(
    @NotBlank(message = "Username is required")
    String username,
    
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    String email,
    
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password,
    
    @NotNull(message = "Role is required")
    UserRole role,
    
    @NotBlank(message = "Department is required")
    String department
) {}
