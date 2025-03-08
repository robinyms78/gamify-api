package sg.edu.ntu.gamify_demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import sg.edu.ntu.gamify_demo.commands.TaskEventCommand;
import sg.edu.ntu.gamify_demo.events.EventPublisher;
import sg.edu.ntu.gamify_demo.factories.TaskEventCommandFactory;
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.models.TaskEvent;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.repositories.TaskEventRepository;
import sg.edu.ntu.gamify_demo.strategies.task.TaskPointsCalculationStrategy;

/**
 * Service for handling task events and related operations.
 * This service uses the Command pattern to process different types of task events.
 */
@Service
public class TaskEventService {
    
    private final TaskEventRepository taskEventRepository;
    private final UserService userService;
    private final TaskEventCommandFactory commandFactory;
    private final TaskPointsCalculationStrategy pointsCalculationStrategy;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor for dependency injection.
     */
    @Autowired
    public TaskEventService(
            TaskEventRepository taskEventRepository,
            UserService userService,
            TaskEventCommandFactory commandFactory,
            TaskPointsCalculationStrategy pointsCalculationStrategy,
            EventPublisher eventPublisher,
            ObjectMapper objectMapper) {
        this.taskEventRepository = taskEventRepository;
        this.userService = userService;
        this.commandFactory = commandFactory;
        this.pointsCalculationStrategy = pointsCalculationStrategy;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Process a task event.
     * 
     * @param userId The ID of the user associated with the event.
     * @param taskId The ID of the task.
     * @param eventType The type of event.
     * @param eventData Additional data about the event.
     * @return The processed task event.
     */
    @Transactional
    public TaskEvent processTaskEvent(String userId, String taskId, String eventType, JsonNode eventData) {
        User user = userService.getUserById(userId);
        
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        
        // Create and execute the appropriate command for this event type
        TaskEventCommand command = commandFactory.createCommand(eventType, user, taskId, eventData);
        TaskEvent taskEvent = command.execute();
        
        // Publish domain event
        if (eventPublisher != null) {
            eventPublisher.publishEvent(eventType, user, eventData);
        }
        
        return taskEvent;
    }
    
    /**
     * Get a task event by its ID.
     * 
     * @param eventId The ID of the event.
     * @return The task event, or null if not found.
     */
    public TaskEvent getTaskEventById(String eventId) {
        return taskEventRepository.findById(eventId).orElse(null);
    }
    
    /**
     * Calculate points for a task based on its priority.
     * This is a convenience method that delegates to the strategy.
     * 
     * @param taskId The ID of the task.
     * @param eventData Additional data about the task.
     * @return The number of points to award.
     */
    public int calculatePointsForTask(String taskId, JsonNode eventData) {
        return pointsCalculationStrategy.calculatePoints(taskId, eventData);
    }
}
