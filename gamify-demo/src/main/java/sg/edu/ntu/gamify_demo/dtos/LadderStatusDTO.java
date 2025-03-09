package sg.edu.ntu.gamify_demo.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for ladder status information.
 * Used to transfer ladder status data between the service layer and the controller.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LadderStatusDTO {
    private int currentLevel;
    private String levelLabel;
    private int earnedPoints;
    private int pointsToNextLevel;
}
