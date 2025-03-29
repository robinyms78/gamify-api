package sg.edu.ntu.gamify_demo.factories;

import java.util.UUID;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import sg.edu.ntu.gamify_demo.models.Achievement;

/**
 * Factory for creating Achievement objects.
 * This class encapsulates the creation logic for achievements.
 */
@Component
public class AchievementFactory {
    
    /**
     * Creates a new Achievement with the provided details.
     * 
     * @param name The name of the achievement.
     * @param description The description of the achievement.
     * @param criteria The criteria for earning this achievement.
     * @return The created Achievement.
     */
    public Achievement createAchievement(String name, String description, JsonNode criteria) {
        Achievement achievement = new Achievement();
        // Let Hibernate generate the ID
        achievement.setName(name);
        achievement.setDescription(description);
        achievement.setCriteria(criteria);
        
        return achievement;
    }
    
    /**
     * Creates a new Achievement with the provided details and a specific ID.
     * Note: This method should only be used in specific cases where the ID needs to be set manually,
     * such as in tests or data migration. For normal operations, use createAchievement() instead
     * to let Hibernate generate the ID.
     * 
     * @param achievementId The ID for the achievement.
     * @param name The name of the achievement.
     * @param description The description of the achievement.
     * @param criteria The criteria for earning this achievement.
     * @return The created Achievement.
     */
    public Achievement createAchievementWithId(String achievementId, String name, String description, JsonNode criteria) {
        Achievement achievement = new Achievement();
        achievement.setAchievementId(achievementId);
        achievement.setName(name);
        achievement.setDescription(description);
        achievement.setCriteria(criteria);
        
        return achievement;
    }
}
