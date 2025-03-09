package sg.edu.ntu.gamify_demo.strategies.task;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Strategy interface for calculating points for task completion.
 * Allows for different implementations of points calculation logic based on task attributes.
 */
public interface TaskPointsCalculationStrategy {
    
    /**
     * Calculate the points to award for completing a task.
     * 
     * @param taskId The ID of the task.
     * @param eventData Additional data about the task.
     * @return The number of points to award.
     */
    int calculatePoints(String taskId, JsonNode eventData);
}
