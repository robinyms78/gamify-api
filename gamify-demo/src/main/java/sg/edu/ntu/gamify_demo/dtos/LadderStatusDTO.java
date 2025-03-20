package sg.edu.ntu.gamify_demo.dtos;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LadderStatusDTO {
    private Integer currentLevel;
    private String levelLabel;
    private Integer earnedPoints;
    private Integer pointsToNextLevel;
    private String userId;
    private String userName;
}
