package sg.edu.ntu.gamify_demo.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(name = "ErrorResponse")
public class ErrorResponse {
    @Schema(example = "Validation error")
    private String error;
    
    @Schema(example = "Username must be at least 4 characters")
    private String message;
}
