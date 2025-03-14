package sg.edu.ntu.gamify_demo.facades;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sg.edu.ntu.gamify_demo.Services.PointsService;
import sg.edu.ntu.gamify_demo.Services.TaskEventService;
import sg.edu.ntu.gamify_demo.dtos.LadderStatusDTO;
import sg.edu.ntu.gamify_demo.dtos.UserAchievementDTO;
import sg.edu.ntu.gamify_demo.events.EventPublisher;
import sg.edu.ntu.gamify_demo.interfaces.AchievementService;
import sg.edu.ntu.gamify_demo.interfaces.LadderStatusService;
import sg.edu.ntu.gamify_demo.interfaces.UserAchievementService;
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.models.Achievement;
import sg.edu.ntu.gamify_demo.models.TaskEvent;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.strategies.task.TaskPointsCalculationStrategy;

/**
 * Facade for gamification-related operations.
 * Simplifies the interaction between controllers and multiple services.
 * This follows the Facade pattern to provide a unified interface for gamification operations.
 */
@Service
public class GamificationFacade {
    
    private final LadderStatusService ladderStatusService;
    private final AchievementService achievementService;
    private final UserAchievementService userAchievementService;
    private final UserService userService;
    private final EventPublisher eventPublisher;
    private final TaskEventService taskEventService;
    private final PointsService pointsService;
    private final TaskPointsCalculationStrategy pointsCalculationStrategy;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor for dependency injection.
     */
    public GamificationFacade(
            LadderStatusService ladderStatusService,
            AchievementService achievementService,
            UserAchievementService userAchievementService,
            UserService userService,
            EventPublisher eventPublisher,
            TaskEventService taskEventService,
            PointsService pointsService,
            TaskPointsCalculationStrategy pointsCalculationStrategy,
            ObjectMapper objectMapper) {
        this.ladderStatusService = ladderStatusService;
        this.achievementService = achievementService;
        this.userAchievementService = userAchievementService;
        this.userService = userService;
        this.eventPublisher = eventPublisher;
        this.taskEventService = taskEventService;
        this.pointsService = pointsService;
        this.pointsCalculationStrategy = pointsCalculationStrategy;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Process a task event and handle all related gamification operations.
     * This method provides a unified interface for task event processing.
     * 
     * @param userId The ID of the user associated with the event.
     * @param taskId The ID of the task.
     * @param eventType The type of event.
     * @param eventData Additional data about the event.
     * @return The processed task event.
     */
    @Transactional
    public TaskEvent processTaskEvent(String userId, String taskId, String eventType, JsonNode eventData) {
        // Create a JSON object with the required fields
        ObjectNode requestData = objectMapper.createObjectNode();
        requestData.put("userId", userId);
        requestData.put("taskId", taskId);
        requestData.put("event_type", eventType);
        requestData.set("data", eventData);
        
        // Process the task event
        ObjectNode response = taskEventService.processTaskEvent(requestData);
        
        // Get the event ID from the response
        String eventId = response.get("eventId").asText();
        
        // Retrieve the task event by ID
        TaskEvent taskEvent = taskEventService.getTaskEventById(eventId);
        
        // If this is a task completion event, award points
        if ("TASK_COMPLETED".equals(eventType)) {
            // Calculate points based on task priority
            int points = pointsCalculationStrategy.calculatePoints(taskId, eventData);
            
            // Create metadata for the points transaction
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("taskId", taskId);
            metadata.put("eventId", taskEvent.getEventId());
            metadata.put("eventType", "TASK_COMPLETED");
            
            if (eventData.has("priority")) {
                metadata.put("priority", eventData.get("priority").asText());
            }
            
            // Award points to the user
            pointsService.awardPoints(userId, points, "TASK_COMPLETED", metadata);
            
            // Update the user's ladder status
            updateUserLadderStatus(userId);
        }
        
        return taskEvent;
    }
    
    /**
     * Get a user's current ladder status.
     * 
     * @param userId The ID of the user.
     * @return The user's ladder status as a DTO, or null if the user doesn't exist.
     */
    public LadderStatusDTO getUserLadderStatus(String userId) {
        return ladderStatusService.getUserLadderStatus(userId);
    }
    
    /**
     * Update a user's ladder status based on their earned points.
     * 
     * @param userId The ID of the user.
     * @return The updated ladder status as a DTO, or null if the user doesn't exist.
     */
    public LadderStatusDTO updateUserLadderStatus(String userId) {
        return ladderStatusService.updateUserLadderStatus(userId);
    }
    
    /**
     * Get a user's achievements.
     * 
     * @param userId The ID of the user.
     * @return The user's achievements as a DTO.
     */
    public UserAchievementDTO getUserAchievements(String userId) {
        return userAchievementService.getUserAchievementsDTO(userId);
    }
    
    /**
     * Get all achievements.
     * 
     * @return A list of all achievements.
     */
    public List<Achievement> getAllAchievements() {
        return achievementService.getAllAchievements();
    }
    
    /**
     * Award points to a user.
     * 
     * @param userId The ID of the user.
     * @param points The number of points to award.
     * @param source The source of the points.
     * @param metadata Additional data about the points transaction.
     * @return The user's new total earned points.
     */
    @Transactional
    public int awardPoints(String userId, int points, String source, JsonNode metadata) {
        return pointsService.awardPoints(userId, points, source, metadata);
    }
    
    /**
     * Process an event for a user.
     * This method publishes the event to all registered listeners.
     * 
     * @param eventType The type of event.
     * @param userId The ID of the user.
     * @param eventData Additional data about the event.
     */
    public void processEvent(String eventType, String userId, JsonNode eventData) {
        User user = userService.getUserById(userId);
        
        if (user != null) {
            eventPublisher.publishEvent(eventType, user, eventData);
        }
    }
}
