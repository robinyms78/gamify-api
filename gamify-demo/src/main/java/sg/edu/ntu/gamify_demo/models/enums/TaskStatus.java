package sg.edu.ntu.gamify_demo.models.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum representing the possible statuses of a task.
 * Corresponds to the 'task_status' ENUM type in the database.
 */
@Schema(description = "Represents the possible statuses of a task")
public enum TaskStatus {
    @Schema(description = "Task has been assigned to the user", example = "ASSIGNED")
    ASSIGNED("assigned"),
    
    @Schema(description = "Task is currently in progress", example = "IN_PROGRESS")
    IN_PROGRESS("in_progress"),
    
    @Schema(description = "Task has been completed", example = "COMPLETED")
    COMPLETED("completed"),
    
    @Schema(description = "Task has been cancelled", example = "CANCELLED")
    CANCELLED("cancelled");
    
    private final String value;

    TaskStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TaskStatus fromValue(String value) {
        for (TaskStatus status : TaskStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown task status: " + value);
    }
}
