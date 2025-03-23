package sg.edu.ntu.gamify_demo.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.Instant;

@Data
@Schema(name = "ErrorResponse", description = "Standard error response structure")
public class ErrorResponseDTO {
    @Schema(description = "Error type category", example = "Bad Request", requiredMode = Schema.RequiredMode.REQUIRED)
    private String error;
    
    @Schema(description = "Detailed error message", example = "Missing required field: userId", requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;
    
    @Schema(description = "Timestamp of error occurrence", example = "2024-03-15T14:30:45Z", requiredMode = Schema.RequiredMode.REQUIRED)
    private Instant timestamp = Instant.now();
}
