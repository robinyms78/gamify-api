package sg.edu.ntu.gamify_demo.commands;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sg.edu.ntu.gamify_demo.services.PointsService;
import sg.edu.ntu.gamify_demo.events.domain.DomainEventPublisher;
import sg.edu.ntu.gamify_demo.events.domain.TaskCompletedEvent;
import sg.edu.ntu.gamify_demo.models.TaskEvent;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.enums.TaskStatus;
import sg.edu.ntu.gamify_demo.repositories.TaskEventRepository;
import sg.edu.ntu.gamify_demo.strategies.task.TaskPointsCalculationStrategy;

/**
 * Command implementation for processing task completion events.
 * This follows the Command pattern to encapsulate task completion logic.
 */
public class TaskCompletedCommand implements TaskEventCommand {
    
    private final User user;
    private final String taskId;
    private final JsonNode eventData;
    private final TaskEventRepository taskEventRepository;
    private final TaskPointsCalculationStrategy pointsCalculationStrategy;
    private final PointsService pointsService;
    private final ObjectMapper objectMapper;
    private final DomainEventPublisher domainEventPublisher;
    
    /**
     * Constructor for the TaskCompletedCommand.
     * 
     * @param user The user who completed the task.
     * @param taskId The ID of the completed task.
     * @param eventData Additional data about the task.
     * @param taskEventRepository Repository for task events.
     * @param pointsCalculationStrategy Strategy for calculating points.
     * @param pointsService Service for awarding points.
     * @param objectMapper Object mapper for JSON manipulation.
     * @param domainEventPublisher Publisher for domain events.
     */
    public TaskCompletedCommand(
            User user,
            String taskId,
            JsonNode eventData,
            TaskEventRepository taskEventRepository,
            TaskPointsCalculationStrategy pointsCalculationStrategy,
            PointsService pointsService,
            ObjectMapper objectMapper,
            DomainEventPublisher domainEventPublisher) {
        this.user = user;
        this.taskId = taskId;
        this.eventData = eventData;
        this.taskEventRepository = taskEventRepository;
        this.pointsCalculationStrategy = pointsCalculationStrategy;
        this.pointsService = pointsService;
        this.objectMapper = objectMapper;
        this.domainEventPublisher = domainEventPublisher;
    }
    
    @Override
    public TaskEvent execute() {
        // Create a new task event
        TaskEvent taskEvent = new TaskEvent();
        taskEvent.setEventId(UUID.randomUUID().toString());
        taskEvent.setUser(user);
        taskEvent.setTaskId(taskId);
        taskEvent.setEventType("TASK_COMPLETED");
        taskEvent.setMetadata(eventData);
        taskEvent.setStatus(TaskStatus.COMPLETED);
        taskEvent.setCompletionTime(ZonedDateTime.now());
        
        // Save the task event
        TaskEvent savedEvent = taskEventRepository.save(taskEvent);
        
        // Calculate points based on task priority
        Long points = (long) pointsCalculationStrategy.calculatePoints(taskId, eventData);
        
        // Create metadata for the points transaction
        ObjectNode metadata = objectMapper.createObjectNode();
        metadata.put("taskId", taskId);
        metadata.put("eventId", savedEvent.getEventId());
        metadata.put("eventType", "TASK_COMPLETED");
        
        if (eventData.has("priority")) {
            metadata.put("priority", eventData.get("priority").asText());
        }
        
        // Award points to the user
        pointsService.awardPoints(user.getId(), points, "TASK_COMPLETED", metadata);
        
        // Publish domain event
        if (domainEventPublisher != null) {
            TaskCompletedEvent domainEvent = new TaskCompletedEvent(
                    "TASK_COMPLETED",
                    user,
                    savedEvent,
                    points.intValue(),
                    eventData);
            
            domainEventPublisher.publish(domainEvent);
        }
        
        return savedEvent;
    }
}
