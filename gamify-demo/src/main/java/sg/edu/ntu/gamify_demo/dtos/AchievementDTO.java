package sg.edu.ntu.gamify_demo.dtos;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Achievement information.
 * Used to transfer achievement data between layers and to the client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "Achievement", description = "Defines an achievement and its unlock status for a user")
public class AchievementDTO {
    
    @Schema(description = "Unique achievement ID", 
           example = "achieve-1122",
           pattern = "^achieve-[a-zA-Z0-9]{8}$")
    private String id;
    
    @Schema(description = "Achievement name", 
           example = "Code Wizard",
           minLength = 5,
           maxLength = 50)
    private String name;
    
    @Schema(description = "Detailed description", 
           example = "Complete 100 programming challenges",
           minLength = 10,
           maxLength = 255)
    private String description;
    
    @Schema(description = "Timestamp when earned (null if not earned)", 
           example = "2024-03-20T14:30:00Z")
    private LocalDateTime earnedAt;
    
    @Schema(description = "Additional achievement metadata", 
           example = "{\"difficulty\": \"hard\", \"category\": \"coding\"}")
    private JsonNode metadata;
    
    @Schema(description = "Whether the achievement has been earned", 
           example = "true")
    private boolean earned;
}
