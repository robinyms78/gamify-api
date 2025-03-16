package sg.edu.ntu.gamify_demo.factories;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import sg.edu.ntu.gamify_demo.commands.CalculatePointsCommand;
import sg.edu.ntu.gamify_demo.commands.CompositeTaskCommand;
import sg.edu.ntu.gamify_demo.commands.TaskAssignedCommand;
import sg.edu.ntu.gamify_demo.commands.TaskEventCommand;
import sg.edu.ntu.gamify_demo.commands.TestRecordTransactionCommand;
import sg.edu.ntu.gamify_demo.commands.TestUpdateLadderStatusCommand;
import sg.edu.ntu.gamify_demo.interfaces.LadderStatusService;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.repositories.PointsTransactionRepository;
import sg.edu.ntu.gamify_demo.repositories.TaskEventRepository;
import sg.edu.ntu.gamify_demo.strategies.task.TaskPointsCalculationStrategy;

/**
 * Test-specific implementation of TaskEventCommandFactory that uses TestRecordTransactionCommand.
 * This is used to avoid issues with the event system in tests.
 */
@Component
public class TestTaskEventCommandFactory {
    
    private final TaskEventRepository taskEventRepository;
    private final PointsTransactionRepository pointsTransactionRepository;
    private final TaskPointsCalculationStrategy pointsCalculationStrategy;
    private final LadderStatusService ladderStatusService;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor for dependency injection.
     */
    public TestTaskEventCommandFactory(
            TaskEventRepository taskEventRepository,
            PointsTransactionRepository pointsTransactionRepository,
            TaskPointsCalculationStrategy pointsCalculationStrategy,
            LadderStatusService ladderStatusService,
            ObjectMapper objectMapper) {
        this.taskEventRepository = taskEventRepository;
        this.pointsTransactionRepository = pointsTransactionRepository;
        this.pointsCalculationStrategy = pointsCalculationStrategy;
        this.ladderStatusService = ladderStatusService;
        this.objectMapper = objectMapper;
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
                
                // Use TestRecordTransactionCommand instead of RecordTransactionCommand
                TestRecordTransactionCommand recordTransactionCommand = new TestRecordTransactionCommand(
                        user,
                        taskId,
                        eventData,
                        points,
                        pointsTransactionRepository,
                        objectMapper);
                
                // Use TestUpdateLadderStatusCommand instead of UpdateLadderStatusCommand
                TestUpdateLadderStatusCommand updateLadderStatusCommand = new TestUpdateLadderStatusCommand(user);
                
                // Create and return a composite command that executes all commands in a transaction
                return new CompositeTaskCommand(
                        calculatePointsCommand,
                        recordTransactionCommand,
                        updateLadderStatusCommand,
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
