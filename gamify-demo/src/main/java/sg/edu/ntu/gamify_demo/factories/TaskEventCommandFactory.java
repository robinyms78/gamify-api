package sg.edu.ntu.gamify_demo.factories;

import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import sg.edu.ntu.gamify_demo.services.PointsService;
import sg.edu.ntu.gamify_demo.commands.CalculatePointsCommand;
import sg.edu.ntu.gamify_demo.commands.CompositeTaskCommand;
import sg.edu.ntu.gamify_demo.commands.RecordTransactionCommand;
import sg.edu.ntu.gamify_demo.commands.TaskAssignedCommand;
import sg.edu.ntu.gamify_demo.commands.TaskEventCommand;
import sg.edu.ntu.gamify_demo.commands.UpdateLadderStatusCommand;
import sg.edu.ntu.gamify_demo.events.domain.DomainEventPublisher;
import sg.edu.ntu.gamify_demo.interfaces.LadderStatusService;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.repositories.PointsTransactionRepository;
import sg.edu.ntu.gamify_demo.repositories.TaskEventRepository;
import sg.edu.ntu.gamify_demo.strategies.task.TaskPointsCalculationStrategy;

/**
 * Factory for creating task event commands based on event type.
 * This follows the Factory pattern to encapsulate command creation logic.
 */
@Component
public class TaskEventCommandFactory {
    
    private final TaskEventRepository taskEventRepository;
    private final PointsTransactionRepository pointsTransactionRepository;
    private final TaskPointsCalculationStrategy pointsCalculationStrategy;
    private final PointsService pointsService;
    private final LadderStatusService ladderStatusService;
    private final ObjectMapper objectMapper;
    private final DomainEventPublisher domainEventPublisher;
    
    /**
     * Constructor for dependency injection.
     */
    public TaskEventCommandFactory(
            TaskEventRepository taskEventRepository,
            PointsTransactionRepository pointsTransactionRepository,
            TaskPointsCalculationStrategy pointsCalculationStrategy,
            PointsService pointsService,
            LadderStatusService ladderStatusService,
            ObjectMapper objectMapper,
            DomainEventPublisher domainEventPublisher) {
        this.taskEventRepository = taskEventRepository;
        this.pointsTransactionRepository = pointsTransactionRepository;
        this.pointsCalculationStrategy = pointsCalculationStrategy;
        this.pointsService = pointsService;
        this.ladderStatusService = ladderStatusService;
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
                // Create individual commands
                CalculatePointsCommand calculatePointsCommand = new CalculatePointsCommand(
                        user,
                        taskId,
                        eventData,
                        taskEventRepository,
                        pointsCalculationStrategy);
                
                // Get the points from the calculation command
                Long points = calculatePointsCommand.getCalculatedPoints();
                
                RecordTransactionCommand recordTransactionCommand = new RecordTransactionCommand(
                        user,
                        taskId,
                        eventData,
                        points,
                        pointsTransactionRepository,
                        pointsService,
                        domainEventPublisher,
                        objectMapper);
                
                // Check if ladder status update should be skipped
                boolean skipLadderUpdate = false;
                if (eventData.has("skip_ladder_update")) {
                    skipLadderUpdate = eventData.get("skip_ladder_update").asBoolean(false);
                }
                
                UpdateLadderStatusCommand updateLadderStatusCommand = skipLadderUpdate ? null : 
                        new UpdateLadderStatusCommand(user, ladderStatusService);
                
                // Create and return a composite command that executes all commands in a transaction
                return new CompositeTaskCommand(
                        calculatePointsCommand,
                        recordTransactionCommand,
                        updateLadderStatusCommand, // Include ladder status update if not skipped
                        user,
                        taskId,
                        eventData);
                
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
