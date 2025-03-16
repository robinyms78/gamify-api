package sg.edu.ntu.gamify_demo.commands;

import sg.edu.ntu.gamify_demo.models.TaskEvent;
import sg.edu.ntu.gamify_demo.models.User;

/**
 * Test-specific implementation of UpdateLadderStatusCommand that doesn't actually update the ladder status.
 * This is used to avoid issues with the ladder status update in tests.
 */
public class TestUpdateLadderStatusCommand implements TaskEventCommand {
    
    private final User user;
    
    /**
     * Constructor for the command.
     * 
     * @param user The user executing the command.
     */
    public TestUpdateLadderStatusCommand(User user) {
        this.user = user;
    }
    
    /**
     * Execute the command to update the ladder status.
     * In this test-specific implementation, we don't actually update the ladder status.
     * 
     * @return A task event (null in this case, as we're not creating a task event).
     */
    @Override
    public TaskEvent execute() {
        // Do nothing
        return null;
    }
}
