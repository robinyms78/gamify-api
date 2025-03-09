package sg.edu.ntu.gamify_demo.interfaces;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import sg.edu.ntu.gamify_demo.models.Achievement;

/**
 * Service interface for managing achievements.
 */
public interface AchievementService {
    
    /**
     * Create a new achievement.
     * 
     * @param name The name of the achievement.
     * @param description The description of the achievement.
     * @param criteria The criteria for earning this achievement.
     * @return The created Achievement.
     */
    Achievement createAchievement(String name, String description, JsonNode criteria);
    
    /**
     * Get an achievement by its ID.
     * 
     * @param achievementId The ID of the achievement.
     * @return The Achievement if found, null otherwise.
     */
    Achievement getAchievementById(String achievementId);
    
    /**
     * Get an achievement by its name.
     * 
     * @param name The name of the achievement.
     * @return The Achievement if found, null otherwise.
     */
    Achievement getAchievementByName(String name);
    
    /**
     * Get all achievements.
     * 
     * @return A list of all achievements.
     */
    List<Achievement> getAllAchievements();
    
    /**
     * Update an existing achievement.
     * 
     * @param achievementId The ID of the achievement to update.
     * @param name The new name of the achievement.
     * @param description The new description of the achievement.
     * @param criteria The new criteria for earning this achievement.
     * @return The updated Achievement.
     */
    Achievement updateAchievement(String achievementId, String name, String description, JsonNode criteria);
    
    /**
     * Delete an achievement.
     * 
     * @param achievementId The ID of the achievement to delete.
     */
    void deleteAchievement(String achievementId);
}
