package sg.edu.ntu.gamify_demo.dtos;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(name = "EventProcessResult", description = "Result of event processing")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventProcessResponse {
    @Schema(description = "Success status", example = "true")
    private boolean success;
    
    @Schema(description = "Newly unlocked achievements",
           example = "[\"achieve-123\"]")
    private List<String> newAchievements;
    
    @Schema(description = "Total achievements unlocked", example = "5")
    private int totalAchievements;
    
    @Schema(description = "Completion percentage", 
           example = "58.3",
           maximum = "100.0")
    private double completionPercentage;
}
