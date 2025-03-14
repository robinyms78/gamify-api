package sg.edu.ntu.gamify_demo.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sg.edu.ntu.gamify_demo.commands.TaskEventCommand;
import sg.edu.ntu.gamify_demo.events.domain.DomainEventPublisher;
import sg.edu.ntu.gamify_demo.events.domain.TaskCompletedEvent;
import sg.edu.ntu.gamify_demo.factories.TaskEventCommandFactory;
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.models.TaskEvent;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.repositories.TaskEventRepository;
import sg.edu.ntu.gamify_demo.strategies.task.TaskPointsCalculationStrategy;

/**
 * Service for handling task events.
 * This service follows the Command pattern to process different types of task events.
 */
@Service
public class TaskEventService {

    private final TaskEventRepository taskEventRepository;
    private final UserService userService;
    private final TaskEventCommandFactory commandFactory;
    private final TaskPointsCalculationStrategy pointsCalculationStrategy;
    private final DomainEventPublisher domainEventPublisher;
    private final ObjectMapper objectMapper;

    /**
     * Constructor for dependency injection.
     */
    public TaskEventService(TaskEventRepository taskEventRepository,
                            UserService userService,
                            TaskEventCommandFactory commandFactory,
                            TaskPointsCalculationStrategy pointsCalculationStrategy,
                            DomainEventPublisher domainEventPublisher,
                            ObjectMapper objectMapper) {
        this.taskEventRepository = taskEventRepository;
        this.userService = userService;
        this.commandFactory = commandFactory;
        this.pointsCalculationStrategy = pointsCalculationStrategy;
        this.domainEventPublisher = domainEventPublisher;
        this.objectMapper = objectMapper;
    }

    /**
     * Process a task event.
     * 
     * @param eventData JSON data containing userId, taskId, eventType, and additional event data.
     * @return A response containing information about the processed event.
     * @throws IllegalArgumentException if required fields are missing or invalid.
     */
    @Transactional
    public ObjectNode processTaskEvent(JsonNode eventData) {
        // Extract and validate required fields
        String userId = eventData.has("userId") ? eventData.get("userId").asText() : null;
        String taskId = eventData.has("taskId") ? eventData.get("taskId").asText() : null;
        String eventType = eventData.has("event_type") ? eventData.get("event_type").asText() : null;
        JsonNode additionalData = eventData.has("data") ? eventData.get("data") : objectMapper.createObjectNode();

        if (userId == null || taskId == null || eventType == null) {
            throw new IllegalArgumentException("Missing required fields: userId, taskId, and event_type");
        }

        User user = userService.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }

        // Execute the appropriate command for this event type
        TaskEventCommand command = commandFactory.createCommand(eventType, user, taskId, additionalData);
        TaskEvent taskEvent = command.execute();

        // Prepare response
        ObjectNode response = formatResponse(taskEvent, userId, taskId, eventType);

        // Handle task completion specific logic
        if ("TASK_COMPLETED".equals(eventType)) {
            handleTaskCompletedEvent(user, taskEvent, taskId, additionalData, response);
        }
        
        return response;
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
     * Calculate points for a completed task.
     * 
     * @param taskId The ID of the task.
     * @param eventData Additional data about the task.
     * @return The number of points awarded.
     */
    public int calculatePointsForTask(String taskId, JsonNode eventData) {
        return pointsCalculationStrategy.calculatePoints(taskId, eventData);
    }
    
    /**
     * Format the response for a processed task event.
     * 
     * @param taskEvent The processed task event.
     * @param userId The ID of the user.
     * @param taskId The ID of the task.
     * @param eventType The type of event.
     * @return A formatted response.
     */
    private ObjectNode formatResponse(TaskEvent taskEvent, String userId, String taskId, String eventType) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("success", true);
        response.put("eventId", taskEvent.getEventId());
        response.put("userId", userId);
        response.put("taskId", taskId);
        response.put("eventType", eventType);
        response.put("status", taskEvent.getStatus().toString());
        return response;
    }
    
    /**
     * Handle task completed event specific logic.
     * 
     * @param user The user who completed the task.
     * @param taskEvent The task event.
     * @param taskId The ID of the task.
     * @param additionalData Additional data about the task.
     * @param response The response to update with points information.
     */
    private void handleTaskCompletedEvent(User user, TaskEvent taskEvent, String taskId, 
                                         JsonNode additionalData, ObjectNode response) {
        int pointsAwarded = calculatePointsForTask(taskId, additionalData);
        response.put("pointsAwarded", pointsAwarded);

        // Create and publish a domain event for task completion
        TaskCompletedEvent completedEvent = new TaskCompletedEvent(
                "TASK_COMPLETED", user, taskEvent, pointsAwarded, additionalData
        );
        domainEventPublisher.publish(completedEvent);
    }
}
