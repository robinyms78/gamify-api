package sg.edu.ntu.gamify_demo.factories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import sg.edu.ntu.gamify_demo.commands.TaskAssignedCommand;
import sg.edu.ntu.gamify_demo.commands.TaskCompletedCommand;
import sg.edu.ntu.gamify_demo.commands.TaskEventCommand;
import sg.edu.ntu.gamify_demo.events.domain.DomainEventPublisher;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.repositories.TaskEventRepository;
import sg.edu.ntu.gamify_demo.services.PointsService;
import sg.edu.ntu.gamify_demo.strategies.task.TaskPointsCalculationStrategy;

/**
 * Factory for creating task event commands based on event type.
 * This follows the Factory pattern to encapsulate command creation logic.
 */
@Component
public class TaskEventCommandFactory {
    
    private final TaskEventRepository taskEventRepository;
    private final TaskPointsCalculationStrategy pointsCalculationStrategy;
    private final PointsService pointsService;
    private final ObjectMapper objectMapper;
    private final DomainEventPublisher domainEventPublisher;
    
    /**
     * Constructor for dependency injection.
     */
    @Autowired
    public TaskEventCommandFactory(
            TaskEventRepository taskEventRepository,
            TaskPointsCalculationStrategy pointsCalculationStrategy,
            PointsService pointsService,
            ObjectMapper objectMapper,
            DomainEventPublisher domainEventPublisher) {
        this.taskEventRepository = taskEventRepository;
        this.pointsCalculationStrategy = pointsCalculationStrategy;
        this.pointsService = pointsService;
        this.objectMapper = objectMapper;
        this.domainEventPublisher = domainEventPublisher;
    }
    
    /**
     * Create a command for processing a task event.
     * 
     * @param eventType The type of event.
     * @param user The user associated with the event.
     * @param taskId The ID of the task.
     * @param eventData Additional data about the event.
     * @return The appropriate command for the event type.
     * @throws IllegalArgumentException If the event type is not supported.
     */
    public TaskEventCommand createCommand(String eventType, User user, String taskId, JsonNode eventData) {
        switch (eventType) {
            case "TASK_COMPLETED":
                return new TaskCompletedCommand(
                        user,
                        taskId,
                        eventData,
                        taskEventRepository,
                        pointsCalculationStrategy,
                        pointsService,
                        objectMapper,
                        domainEventPublisher);
            case "TASK_ASSIGNED":
                return new TaskAssignedCommand(
                        user,
                        taskId,
                        eventData,
                        taskEventRepository);
            default:
                throw new IllegalArgumentException("Unsupported event type: " + eventType);
        }
    }
}
