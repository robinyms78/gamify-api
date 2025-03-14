package sg.edu.ntu.gamify_demo.events;

import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import sg.edu.ntu.gamify_demo.interfaces.UserAchievementService;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.UserAchievement;

/**
 * Event listener for processing achievements based on events.
 * This class listens for events and processes achievements accordingly.
 */
@Component
public class AchievementProcessor implements EventListener {

    private final UserAchievementService userAchievementService;
    private final EventPublisher eventPublisher;
    
    private static final String[] INTERESTED_EVENT_TYPES = {
        "TASK_COMPLETED",
        "POINTS_EARNED",
        "USER_LOGGED_IN"
    };
    
    /**
     * Constructor for dependency injection.
     * Registers this processor as a listener with the event publisher.
     * 
     * @param userAchievementService Service for managing user achievements.
     * @param eventPublisher Publisher for events.
     */
    public AchievementProcessor(UserAchievementService userAchievementService, EventPublisher eventPublisher) {
        this.userAchievementService = userAchievementService;
        this.eventPublisher = eventPublisher;
        
        // Register this processor as a listener
        this.eventPublisher.registerListener(this);
    }
    
    @Override
    public void onEvent(String eventType, User user, JsonNode eventData) {
        // Process achievements based on the event
        List<UserAchievement> newAchievements = userAchievementService.processAchievements(user, eventType, eventData);
        
        // Publish achievement earned events for each new achievement
        for (UserAchievement userAchievement : newAchievements) {
            eventPublisher.publishEvent("ACHIEVEMENT_EARNED", user, userAchievement.getMetadata());
        }
    }

    @Override
    public String[] getInterestedEventTypes() {
        return INTERESTED_EVENT_TYPES;
    }
}
