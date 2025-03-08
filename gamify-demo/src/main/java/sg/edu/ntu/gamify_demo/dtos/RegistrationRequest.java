package sg.edu.ntu.gamify_demo.dtos;

import sg.edu.ntu.gamify_demo.models.enums.UserRole;

/**
 * Data Transfer Object for registration requests.
 * Contains all necessary information to register a new user.
 */
public record RegistrationRequest(
    String username,
    String email,
    String password,
    UserRole role,
    String department
) {}
