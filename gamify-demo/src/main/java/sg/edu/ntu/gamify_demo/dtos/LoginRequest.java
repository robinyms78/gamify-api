package sg.edu.ntu.gamify_demo.dtos;

/**
 * Data Transfer Object for login requests.
 * Contains username and password for authentication.
 */
public record LoginRequest(
    String username,
    String password
) {}
