package sg.edu.ntu.gamify_demo.commands;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

import sg.edu.ntu.gamify_demo.models.TaskEvent;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.enums.TaskStatus;
import sg.edu.ntu.gamify_demo.repositories.TaskEventRepository;
import sg.edu.ntu.gamify_demo.strategies.task.TaskPointsCalculationStrategy;

/**
 * Command to calculate points for a completed task.
 * This follows the Command pattern to encapsulate the points calculation logic.
 */
public class CalculatePointsCommand implements TaskEventCommand {
    
    private final User user;
    private final String taskId;
    private final JsonNode eventData;
    private final TaskEventRepository taskEventRepository;
    private final TaskPointsCalculationStrategy pointsCalculationStrategy;
    
    /**
     * Constructor for the command.
     * 
     * @param user The user executing the command.
     * @param taskId The ID of the task.
     * @param eventData Additional data about the event.
     * @param taskEventRepository Repository for task events.
     * @param pointsCalculationStrategy Strategy for calculating points.
     */
    public CalculatePointsCommand(
            User user,
            String taskId,
            JsonNode eventData,
            TaskEventRepository taskEventRepository,
            TaskPointsCalculationStrategy pointsCalculationStrategy) {
        this.user = user;
        this.taskId = taskId;
        this.eventData = eventData;
        this.taskEventRepository = taskEventRepository;
        this.pointsCalculationStrategy = pointsCalculationStrategy;
    }
    
    /**
     * Execute the command to calculate points and create a task event.
     * 
     * @return The created task event.
     */
    @Override
    public TaskEvent execute() {
        // Create and save the task event
        TaskEvent taskEvent = new TaskEvent();
        taskEvent.setEventId(UUID.randomUUID().toString());
        taskEvent.setUser(user);
        taskEvent.setTaskId(taskId);
        taskEvent.setEventType("TASK_COMPLETED");
        taskEvent.setStatus(TaskStatus.COMPLETED);
        taskEvent.setCompletionTime(ZonedDateTime.now());
        taskEvent.setMetadata(eventData);
        
        // Calculate points but don't store them in the TaskEvent
        // (we'll use them in the PointsEarnedEvent)
        long points = pointsCalculationStrategy.calculatePoints(taskId, eventData);
        
        // Save the calculated points in the task event
        taskEvent.setPointsEarned(points);
        return taskEventRepository.save(taskEvent);
    }
    
    /**
     * Get the calculated points for this task.
     * 
     * @return The calculated points.
     */
    public Long getCalculatedPoints() {
        return (long) pointsCalculationStrategy.calculatePoints(taskId, eventData);
    }
}
