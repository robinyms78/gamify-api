package sg.edu.ntu.gamify_demo.dtos;

import sg.edu.ntu.gamify_demo.models.User;
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
    
    public void setUserId(User user) {
        if (user != null && user.getId() != null) {
            this.userId = user.getId(); // Assuming `userId` is a field in this class
        } else {
            throw new IllegalArgumentException("User or user ID is null");
        }
    }

    public void setUserId(Object user) {
        if (user instanceof User) {
            this.userId = ((User) user).getId(); // Assuming User has a getId() method
        } else {
            throw new IllegalArgumentException("Invalid user object");
        }
    }
}
