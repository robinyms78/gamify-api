package sg.edu.ntu.gamify_demo.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sg.edu.ntu.gamify_demo.interfaces.AchievementService;
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.UserAchievement;

/**
 * Service for gamification features including points, achievements, and notifications.
 */
@Service
public class GamificationService {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AchievementService achievementService;
    
    @Autowired
    private MessageBrokerService messageBroker;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Get a user's earned points.
     * 
     * @param userId The ID of the user.
     * @return The user's earned points.
     */
    public int getUserPoints(String userId) {
        User user = userService.getUserById(userId);
        return user != null ? user.getEarnedPoints() : 0;
    }
    
    /**
     * Award points to a user and process any achievements they may have earned.
     * 
     * @param userId The ID of the user.
     * @param points The number of points to award.
     * @param eventType The type of event that triggered the points award.
     * @param eventData Additional data about the event.
     * @return The user's new total earned points.
     */
    @Transactional
    public int awardPoints(String userId, int points, String eventType, JsonNode eventData) {
        User user = userService.getUserById(userId);
        
        if (user == null) {
            return 0;
        }
        
        // Update user's points
        int currentPoints = user.getEarnedPoints();
        int newPoints = currentPoints + points;
        user.setEarnedPoints(newPoints);
        user.setAvailablePoints(user.getAvailablePoints() + points);
        userService.updateUser(user.getId(), user);
        
        // Process achievements
        processAchievements(user, eventType, eventData);
        
        // Notify about points earned
        if (messageBroker != null) {
            ObjectNode notification = objectMapper.createObjectNode();
            notification.put("userId", userId);
            notification.put("eventType", "POINTS_EARNED");
            notification.put("points", points);
            notification.put("newTotal", newPoints);
            
            messageBroker.sendNotification("points", notification);
        }
        
        return newPoints;
    }
    
    /**
     * Process achievements for a user based on an event.
     * 
     * @param user The user to process achievements for.
     * @param eventType The type of event that triggered the check.
     * @param eventData Additional data about the event.
     * @return A list of newly awarded UserAchievement objects.
     */
    public List<UserAchievement> processAchievements(User user, String eventType, JsonNode eventData) {
        List<UserAchievement> newAchievements = achievementService.processAchievements(user, eventType, eventData);
        
        // Notify about achievements earned
        if (!newAchievements.isEmpty() && messageBroker != null) {
            for (UserAchievement ua : newAchievements) {
                ObjectNode notification = objectMapper.createObjectNode();
                notification.put("userId", user.getId());
                notification.put("eventType", "ACHIEVEMENT_EARNED");
                notification.put("achievementId", ua.getAchievement().getAchievementId());
                notification.put("achievementName", ua.getAchievement().getName());
                
                messageBroker.sendNotification("achievements", notification);
            }
        }
        
        return newAchievements;
    }
    
    /**
     * Spend points from a user's available points.
     * 
     * @param userId The ID of the user.
     * @param points The number of points to spend.
     * @param eventType The type of event that triggered the points spend.
     * @param eventData Additional data about the event.
     * @return True if the points were successfully spent, false if the user doesn't have enough points.
     */
    @Transactional
    public boolean spendPoints(String userId, int points, String eventType, JsonNode eventData) {
        User user = userService.getUserById(userId);
        
        if (user == null) {
            return false;
        }
        
        int availablePoints = user.getAvailablePoints();
        
        if (availablePoints < points) {
            return false;
        }
        
        // Update user's available points
        user.setAvailablePoints(availablePoints - points);
        userService.updateUser(user.getId(), user);
        
        // Notify about points spent
        if (messageBroker != null) {
            ObjectNode notification = objectMapper.createObjectNode();
            notification.put("userId", userId);
            notification.put("eventType", "POINTS_SPENT");
            notification.put("points", points);
            notification.put("newTotal", user.getAvailablePoints());
            
            messageBroker.sendNotification("points", notification);
        }
        
        return true;
    }
}
