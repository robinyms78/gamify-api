package sg.edu.ntu.gamify_demo.dtos;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(name = "TaskEventRequest", description = "Request payload for processing task events")
public class TaskEventRequestDTO {
    @Schema(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "userId is required")
    private String userId;

    @Schema(description = "Task ID", example = "task-789", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "taskId is required")
    private String taskId;

    @Schema(description = "Type of event", example = "TASK_COMPLETED", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "eventType is required")
    private String eventType;

    @Schema(description = "Additional event-specific data")
    private JsonNode data;
}
