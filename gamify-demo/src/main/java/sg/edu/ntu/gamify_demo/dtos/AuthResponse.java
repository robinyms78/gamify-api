package sg.edu.ntu.gamify_demo.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import sg.edu.ntu.gamify_demo.models.User;

@Schema(name = "AuthResponse", description = "Authentication response containing JWT token and user details")
public record AuthResponse(
    @Schema(description = "JWT access token for API calls", 
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
    String token,
    
    @Schema(description = "Authenticated user details", 
            implementation = User.class)
    User user
) {}
