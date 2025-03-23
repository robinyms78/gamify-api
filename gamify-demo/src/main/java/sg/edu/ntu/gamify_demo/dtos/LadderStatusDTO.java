package sg.edu.ntu.gamify_demo.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "LadderStatus", description = "Represents a user's progression in the leveling system")
public class LadderStatusDTO {
    @Schema(description = "Current level number", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer currentLevel;

    @Schema(description = "Display label for current level", example = "Seasoned Adventurer", requiredMode = Schema.RequiredMode.REQUIRED)
    private String levelLabel;

    @Schema(description = "Total earned points", example = "750", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer earnedPoints;

    @Schema(description = "Points needed for next level", example = "250", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer pointsToNextLevel;

    @Schema(description = "Unique user identifier", example = "user-12345", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userId;

    @Schema(description = "User's display name", example = "john_doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userName;
}
