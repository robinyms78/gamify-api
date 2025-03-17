package sg.edu.ntu.gamify_demo.dtos;

import lombok.Data;

@Data
public class TaskEventResponseDTO {
    private String eventId;
    private String userId;
    private String taskId;
    private String eventType;
    private Integer pointsAwarded;
    private String priority;
}
