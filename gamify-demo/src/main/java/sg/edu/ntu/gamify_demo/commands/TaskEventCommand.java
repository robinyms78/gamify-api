package sg.edu.ntu.gamify_demo.commands;

import sg.edu.ntu.gamify_demo.models.TaskEvent;

/**
 * Command interface for processing task events.
 * This follows the Command pattern to encapsulate task event processing logic.
 */
public interface TaskEventCommand {
    
    /**
     * Execute the command to process a task event.
     * 
     * @return The processed task event.
     */
    TaskEvent execute();
}
