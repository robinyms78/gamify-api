package sg.edu.ntu.gamify_demo.dtos;

import sg.edu.ntu.gamify_demo.models.enums.TaskStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskEventDTO {
    private String eventId;
    private String userId;
    private String taskId;
    private String eventType;
    private TaskStatus status;
    private LocalDateTime completionTime;
}
