package sg.edu.ntu.gamify_demo.dtos;

import sg.edu.ntu.gamify_demo.models.enums.TaskStatus;
import lombok.Data;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(name = "TaskEventDTO", description = "Data Transfer Object for Task Events")
public class TaskEventDTO {
    @Schema(description = "Event ID", example = "event-12345")
    private String eventId;
    
    @Schema(description = "User ID", example = "user-56789")
    private String userId;
    
    @Schema(description = "Task ID", example = "task-789")
    private String taskId;
    
    @Schema(description = "Event type", example = "TASK_COMPLETED")
    private String eventType;
    
    @Schema(description = "Task status", example = "COMPLETED")
    private TaskStatus status;
    
    @Schema(description = "Completion time", example = "2025-03-23T23:14:00")
    private LocalDateTime completionTime;
}
