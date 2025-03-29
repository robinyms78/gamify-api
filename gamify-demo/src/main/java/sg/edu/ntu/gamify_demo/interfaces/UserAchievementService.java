package sg.edu.ntu.gamify_demo.interfaces;

import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import sg.edu.ntu.gamify_demo.dtos.UserAchievementDTO;
import sg.edu.ntu.gamify_demo.models.Achievement;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.UserAchievement;

/**
 * Service interface for managing user achievements.
 */
public interface UserAchievementService {
    
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
    
    /**
     * Get a user's achievements as a DTO.
     * 
     * @param userId The ID of the user.
     * @return A UserAchievementDTO containing the user's achievements.
     */
    UserAchievementDTO getUserAchievementsDTO(String userId);
}
