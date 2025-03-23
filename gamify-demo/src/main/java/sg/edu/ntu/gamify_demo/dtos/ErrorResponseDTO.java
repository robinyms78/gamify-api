package sg.edu.ntu.gamify_demo.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "ErrorResponse", description = "Standard error response structure")
public class ErrorResponseDTO {
    @Schema(description = "Error type category", example = "Bad Request")
    private String error;
    
    @Schema(description = "Detailed error message", example = "Missing required field: userId")
    private String message;
}
