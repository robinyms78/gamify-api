package sg.edu.ntu.gamify_demo.factories;

import java.time.ZonedDateTime;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import sg.edu.ntu.gamify_demo.models.Achievement;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.UserAchievement;

/**
 * Factory for creating UserAchievement objects.
 * This class encapsulates the creation logic for user achievements.
 */
@Component
public class UserAchievementFactory {
    
    /**
     * Creates a new UserAchievement with the provided details.
     * 
     * @param user The user who earned the achievement.
     * @param achievement The achievement that was earned.
     * @param metadata Additional data about how the achievement was earned.
     * @return The created UserAchievement.
     */
    public UserAchievement createUserAchievement(User user, Achievement achievement, JsonNode metadata) {
        // Use the builder pattern to ensure all fields are properly set
        return UserAchievement.builder()
            .user(user)
            .achievement(achievement)
            .earnedAt(ZonedDateTime.now())
            .metadata(metadata)
            .build();
    }
    
    /**
     * Creates a new UserAchievement with the provided details and a specific earned time.
     * 
     * @param user The user who earned the achievement.
     * @param achievement The achievement that was earned.
     * @param earnedAt The time when the achievement was earned.
     * @param metadata Additional data about how the achievement was earned.
     * @return The created UserAchievement.
     */
    public UserAchievement createUserAchievementWithTime(User user, Achievement achievement, ZonedDateTime earnedAt, JsonNode metadata) {
        // Use the builder pattern to ensure all fields are properly set
        return UserAchievement.builder()
            .user(user)
            .achievement(achievement)
            .earnedAt(earnedAt)
            .metadata(metadata)
            .build();
    }
}
