package sg.edu.ntu.gamify_demo.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.Instant;

@Data
@AllArgsConstructor
@Schema(name = "ErrorResponse", description = "Standard error response format")
public class ErrorResponse {
    @Schema(description = "Error type category", 
            example = "Not Found", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String error;
    
    @Schema(description = "Detailed error message", 
           example = "User with ID user-12345 not found in the system", 
           requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;
    
    @Schema(description = "Timestamp of error occurrence", 
           example = "2024-03-15T14:30:45Z", 
           requiredMode = Schema.RequiredMode.REQUIRED)
    private Instant timestamp;
}
