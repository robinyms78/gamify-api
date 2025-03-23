package sg.edu.ntu.gamify_demo.dtos;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

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

    @Schema(
        description = "Additional event-specific data that varies based on event type",
        example = """
            {
              "priority": "HIGH",
              "points": 100,
              "details": "Task completed ahead of schedule",
              "metadata": {
                "completedBy": "John Doe",
                "verifiedBy": "Jane Smith"
              }
            }
            """,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private JsonNode data;
}
