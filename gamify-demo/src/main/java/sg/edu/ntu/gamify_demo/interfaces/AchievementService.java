package sg.edu.ntu.gamify_demo.interfaces;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import sg.edu.ntu.gamify_demo.models.Achievement;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.UserAchievement;

/**
 * Service interface for managing achievements and user achievements.
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
    
    /**
     * Award an achievement to a user.
     * 
     * @param user The user to award the achievement to.
     * @param achievement The achievement to award.
     * @param metadata Additional data about how the achievement was earned.
     * @return The created UserAchievement.
     */
    UserAchievement awardAchievement(User user, Achievement achievement, JsonNode metadata);
    
    /**
     * Check if a user has a specific achievement.
     * 
     * @param user The user to check.
     * @param achievement The achievement to check for.
     * @return true if the user has the achievement, false otherwise.
     */
    boolean hasAchievement(User user, Achievement achievement);
    
    /**
     * Get all achievements earned by a user.
     * 
     * @param user The user to get achievements for.
     * @return A list of UserAchievement objects.
     */
    List<UserAchievement> getUserAchievements(User user);
    
    /**
     * Get all users who have earned a specific achievement.
     * 
     * @param achievement The achievement to check.
     * @return A list of UserAchievement objects.
     */
    List<UserAchievement> getAchievementUsers(Achievement achievement);
    
    /**
     * Count the number of achievements a user has earned.
     * 
     * @param user The user to count achievements for.
     * @return The number of achievements earned.
     */
    long countUserAchievements(User user);
    
    /**
     * Check if a user meets the criteria for an achievement.
     * 
     * @param user The user to check.
     * @param achievement The achievement to check criteria for.
     * @return true if the user meets the criteria, false otherwise.
     */
    boolean checkAchievementCriteria(User user, Achievement achievement);
    
    /**
     * Process achievements for a user based on an event.
     * This method checks all achievements and awards any that the user has newly qualified for.
     * 
     * @param user The user to process achievements for.
     * @param eventType The type of event that triggered the check.
     * @param eventData Additional data about the event.
     * @return A list of newly awarded UserAchievement objects.
     */
    List<UserAchievement> processAchievements(User user, String eventType, JsonNode eventData);
}
