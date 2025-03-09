package sg.edu.ntu.gamify_demo.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for User Achievement information.
 * Used to transfer user achievement data between layers and to the client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAchievementDTO {
    
    private String userId;
    private String username;
    private List<AchievementDTO> achievements;
    private int totalAchievements;
    private int earnedAchievements;
}
