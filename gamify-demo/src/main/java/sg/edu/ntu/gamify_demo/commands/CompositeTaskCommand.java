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
        // Execute commands in sequence
        TaskEvent taskEvent = calculatePointsCommand.execute();
        recordTransactionCommand.execute();
        updateLadderStatusCommand.execute();
        return taskEvent;
    }
}
