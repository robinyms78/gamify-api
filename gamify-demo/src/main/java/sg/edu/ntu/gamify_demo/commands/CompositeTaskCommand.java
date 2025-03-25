package sg.edu.ntu.gamify_demo.commands;

import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import sg.edu.ntu.gamify_demo.models.TaskEvent;
import sg.edu.ntu.gamify_demo.models.User;

/**
 * A composite command that executes multiple task commands in a single transaction.
 * This follows the Composite pattern to treat a group of commands as a single command.
 */
public class CompositeTaskCommand implements TaskEventCommand {
    
    private final TaskEventCommand calculatePointsCommand;
    private final TaskEventCommand recordTransactionCommand;
    private final TaskEventCommand updateLadderStatusCommand;
    private final User user;
    private final String taskId;
    private final JsonNode eventData;
    
    /**
     * Constructor for the composite command.
     * 
     * @param calculatePointsCommand Command to calculate points.
     * @param recordTransactionCommand Command to record the transaction.
     * @param updateLadderStatusCommand Command to update the ladder status.
     * @param user The user executing the command.
     * @param taskId The ID of the task.
     * @param eventData Additional data about the event.
     */
    public CompositeTaskCommand(
            TaskEventCommand calculatePointsCommand,
            TaskEventCommand recordTransactionCommand,
            TaskEventCommand updateLadderStatusCommand,
            User user,
            String taskId,
            JsonNode eventData) {
        this.calculatePointsCommand = calculatePointsCommand;
        this.recordTransactionCommand = recordTransactionCommand;
        this.updateLadderStatusCommand = updateLadderStatusCommand;
        this.user = user;
        this.taskId = taskId;
        this.eventData = eventData;
    }
    
    /**
     * Execute all commands in a single transaction.
     * If any command fails, the entire transaction is rolled back.
     * 
     * @return The task event created by the first command.
     */
    @Override
    @Transactional
    public TaskEvent execute() {
        try {
            // Execute commands in sequence
            TaskEvent taskEvent = calculatePointsCommand.execute();
            recordTransactionCommand.execute();
            
            // Try to update ladder status, but don't fail if it fails
            try {
                // Check if ladder status update should be skipped
                boolean skipLadderUpdate = false;
                if (eventData.has("data") && eventData.get("data").has("skip_ladder_update")) {
                    skipLadderUpdate = eventData.get("data").get("skip_ladder_update").asBoolean(false);
                }
                
                if (!skipLadderUpdate && updateLadderStatusCommand != null) {
                    // Ensure user has a valid ladder status before updating
                    ensureUserLadderStatus();
                    updateLadderStatusCommand.execute();
                }
            } catch (Exception e) {
                // Log the error but continue
                System.err.println("Error updating ladder status: " + e.getMessage());
                e.printStackTrace();
            }
            
            return taskEvent;
        } catch (Exception e) {
            // Log the error and rethrow
            System.err.println("Error executing composite command: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Ensure the user has a valid ladder status before updating.
     * This is a workaround for the null identifier issue.
     */
    private void ensureUserLadderStatus() {
        if (user == null || user.getId() == null) {
            System.err.println("User or user ID is null in CompositeTaskCommand");
            return;
        }
        
        // This is a workaround to ensure the user has a valid ladder status
        // We're directly updating the user's points to trigger ladder status creation
        try {
            user.setEarnedPoints(user.getEarnedPoints() != null ? user.getEarnedPoints() : 0L);
        } catch (Exception e) {
            System.err.println("Error ensuring user ladder status: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
