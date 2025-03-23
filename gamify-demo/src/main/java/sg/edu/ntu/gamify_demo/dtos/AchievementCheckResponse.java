package sg.edu.ntu.gamify_demo.dtos;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Detailed achievement progress check response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AchievementCheck", description = "Detailed achievement progress check")
public class AchievementCheckResponse {
    @Schema(description = "Whether achievement is fully earned", example = "false")
    private boolean hasAchievement;
    
    @Schema(description = "Completion progress (0.0-1.0)", example = "0.75")
    private double progress;
    
    @Schema(description = "Missing criteria requirements", 
           example = "[\"tasksCompleted\", \"minimumRating\"]")
    private List<String> requirementsMissing;
    
    @Schema(description = "Current progress values", 
           example = "{\"tasksCompleted\": 75, \"minimumRating\": 4.5}")
    private Map<String, Object> currentProgress;
    
    @Schema(description = "Total required values", 
           example = "{\"tasksCompleted\": 100, \"minimumRating\": 5.0}")
    private Map<String, Object> requiredValues;
}
