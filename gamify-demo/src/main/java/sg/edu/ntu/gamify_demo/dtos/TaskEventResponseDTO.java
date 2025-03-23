package sg.edu.ntu.gamify_demo.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import sg.edu.ntu.gamify_demo.models.enums.TaskStatus;

@Data
@Schema(name = "TaskEventResponse", description = "Response structure for task event processing")
public class TaskEventResponseDTO {
    @Schema(description = "Success status", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean success;
    
    @Schema(description = "Generated event ID", example = "event-12345", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventId;
    
    @Schema(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userId;
    
    @Schema(description = "Task ID", example = "task-789", requiredMode = Schema.RequiredMode.REQUIRED)
    private String taskId;
    
    @Schema(description = "Event type processed", example = "TASK_COMPLETED", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventType;
    
    @Schema(description = "Resulting task status", example = "COMPLETED", requiredMode = Schema.RequiredMode.REQUIRED)
    private TaskStatus status;
    
    @Schema(description = "Priority level if available", example = "HIGH")
    private String priority;
}
