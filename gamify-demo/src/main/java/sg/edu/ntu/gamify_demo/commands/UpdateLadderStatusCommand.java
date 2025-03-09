package sg.edu.ntu.gamify_demo.commands;

import com.fasterxml.jackson.databind.JsonNode;

import sg.edu.ntu.gamify_demo.interfaces.LadderStatusService;
import sg.edu.ntu.gamify_demo.models.TaskEvent;
import sg.edu.ntu.gamify_demo.models.User;

/**
 * Command to update a user's ladder status.
 * This follows the Command pattern to encapsulate the ladder status update logic.
 */
public class UpdateLadderStatusCommand implements TaskEventCommand {
    
    private final User user;
    private final LadderStatusService ladderStatusService;
    
    /**
     * Constructor for the command.
     * 
     * @param user The user whose ladder status to update.
     * @param ladderStatusService Service for managing ladder status.
     */
    public UpdateLadderStatusCommand(
            User user,
            LadderStatusService ladderStatusService) {
        this.user = user;
        this.ladderStatusService = ladderStatusService;
    }
    
    /**
     * Execute the command to update the user's ladder status.
     * 
     * @return A task event (null in this case, as we're not creating a task event).
     */
    @Override
    public TaskEvent execute() {
        // Update the user's ladder status
        ladderStatusService.updateUserLadderStatus(user.getId());
        
        // Return null as we're not creating a task event
        return null;
    }
}
