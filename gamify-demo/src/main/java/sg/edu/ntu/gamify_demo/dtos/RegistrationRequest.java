package sg.edu.ntu.gamify_demo.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import sg.edu.ntu.gamify_demo.models.enums.UserRole;

/**
 * Data Transfer Object for registration requests.
 * Contains all necessary information to register a new user.
 */
public record RegistrationRequest(
    @Schema(description = "Unique username (3-20 chars)", 
            minLength = 3, 
            maxLength = 20,
            example = "john_doe_123")
    @NotBlank(message = "Username is required")
    String username,
    
    @Schema(description = "Valid email address", 
            format = "email",
            example = "john.doe@company.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    String email,
    
    @Schema(description = "Secure password (8+ chars, mix of letters/numbers)", 
            minLength = 8,
            example = "SecurePass123!")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password,
    
    @Schema(description = "User role in system", 
            implementation = UserRole.class,
            example = "employee")
    @NotNull(message = "Role is required")
    UserRole role,
    
    @Schema(description = "Department name", 
            example = "Engineering")
    @NotBlank(message = "Department is required")
    String department
) {}
