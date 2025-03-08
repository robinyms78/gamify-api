package sg.edu.ntu.gamify_demo.events.domain;

import com.fasterxml.jackson.databind.JsonNode;

import sg.edu.ntu.gamify_demo.models.TaskEvent;
import sg.edu.ntu.gamify_demo.models.User;

/**
 * Domain event representing a task completion.
 * This follows the Domain Events pattern to represent a task completion occurrence.
 */
public class TaskCompletedEvent extends DomainEvent {
    
    private final String taskId;
    private final TaskEvent taskEvent;
    private final int pointsAwarded;
    private final JsonNode metadata;
    
    /**
     * Constructor for a task completed event.
     * 
     * @param user The user who completed the task.
     * @param taskId The ID of the completed task.
     * @param taskEvent The task event entity.
     * @param pointsAwarded The number of points awarded for completing the task.
     * @param metadata Additional data about the task.
     */
    public TaskCompletedEvent(
            User user,
            String taskId,
            TaskEvent taskEvent,
            int pointsAwarded,
            JsonNode metadata) {
        super("TASK_COMPLETED", user);
        this.taskId = taskId;
        this.taskEvent = taskEvent;
        this.pointsAwarded = pointsAwarded;
        this.metadata = metadata;
    }
    
    /**
     * Get the ID of the completed task.
     * 
     * @return The task ID.
     */
    public String getTaskId() {
        return taskId;
    }
    
    /**
     * Get the task event entity.
     * 
     * @return The task event.
     */
    public TaskEvent getTaskEvent() {
        return taskEvent;
    }
    
    /**
     * Get the number of points awarded for completing the task.
     * 
     * @return The points awarded.
     */
    public int getPointsAwarded() {
        return pointsAwarded;
    }
    
    /**
     * Get additional data about the task.
     * 
     * @return The metadata.
     */
    public JsonNode getMetadata() {
        return metadata;
    }
}
