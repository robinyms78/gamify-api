package sg.edu.ntu.gamify_demo.dtos;

/**
 * Data Transfer Object for registration responses.
 * Contains a success message and the user ID of the newly created user.
 */
public record RegistrationResponse(
    String message,
    String userId
) {}
