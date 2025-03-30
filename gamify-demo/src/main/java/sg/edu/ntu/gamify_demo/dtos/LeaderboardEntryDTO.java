package sg.edu.ntu.gamify_demo.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Leaderboard entries.
 * Used for API responses to provide leaderboard information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "LeaderboardEntry", description = "Represents a user's position on the leaderboard")
public class LeaderboardEntryDTO {
    
    @Schema(description = "Unique user identifier", example = "user-12345", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userId;
    
    @Schema(description = "User's display name", example = "john_doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;
    
    @Schema(description = "User's department", example = "Engineering", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String department;
    
    @Schema(description = "Total earned points", example = "1500", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer earnedPoints;
    
    @Schema(description = "Current level number", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer currentLevel;
    
    @Schema(description = "Display label for current level", example = "Expert", requiredMode = Schema.RequiredMode.REQUIRED)
    private String levelLabel;
    
    @Schema(description = "User's rank on the leaderboard", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer rank;
}
