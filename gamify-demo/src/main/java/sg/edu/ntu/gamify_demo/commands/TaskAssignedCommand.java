package sg.edu.ntu.gamify_demo.commands;

import java.time.ZonedDateTime;
import java.util.UUID;
import com.fasterxml.jackson.databind.JsonNode;
import sg.edu.ntu.gamify_demo.models.TaskEvent;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.enums.TaskStatus;
import sg.edu.ntu.gamify_demo.repositories.TaskEventRepository;

/**
 * Command implementation for processing task assignment events.
 * This follows the Command pattern to encapsulate task assignment logic.
 */
public class TaskAssignedCommand implements TaskEventCommand {
    
    private final User user;
    private final String taskId;
    private final JsonNode eventData;
    private final TaskEventRepository taskEventRepository;
    
    /**
     * Constructor for the TaskAssignedCommand.
     * 
     * @param user The user who was assigned the task.
     * @param taskId The ID of the assigned task.
     * @param eventData Additional data about the task.
     * @param taskEventRepository Repository for task events.
     */
    public TaskAssignedCommand(
            User user,
            String taskId,
            JsonNode eventData,
            TaskEventRepository taskEventRepository) {
        this.user = user;
        this.taskId = taskId;
        this.eventData = eventData;
        this.taskEventRepository = taskEventRepository;
    }
    
    @Override
    public TaskEvent execute() {
        // Create a new task event
        TaskEvent taskEvent = new TaskEvent();
        taskEvent.setEventId(UUID.randomUUID().toString());
        taskEvent.setUser(user);
        taskEvent.setTaskId(taskId);
        taskEvent.setEventType("TASK_ASSIGNED");
        taskEvent.setMetadata(eventData);
        taskEvent.setStatus(TaskStatus.ASSIGNED);
        taskEvent.setAssignedAt(ZonedDateTime.now());
        
        // Set due date if provided
        if (eventData.has("dueDate")) {
            String dueDateStr = eventData.get("dueDate").asText();
            taskEvent.setDueDate(ZonedDateTime.parse(dueDateStr));
        }
        
        // Save the task event
        return taskEventRepository.save(taskEvent);
    }
}
