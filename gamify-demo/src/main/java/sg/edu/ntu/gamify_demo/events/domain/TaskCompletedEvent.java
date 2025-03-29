package sg.edu.ntu.gamify_demo.events.domain;

import com.fasterxml.jackson.databind.JsonNode;
import sg.edu.ntu.gamify_demo.models.TaskEvent;
import sg.edu.ntu.gamify_demo.models.User;

public class TaskCompletedEvent extends DomainEvent {
    private final String taskId;
    private final int pointsAwarded;
    private final TaskEvent taskEvent;
    private final JsonNode metadata;

    public TaskCompletedEvent(String eventType, User user, TaskEvent taskEvent, int pointsAwarded, JsonNode metadata) {
        super(eventType, user);
        this.taskId = taskEvent.getTaskId();
        this.pointsAwarded = pointsAwarded;
        this.taskEvent = taskEvent;
        this.metadata = metadata;
    }

    public String getTaskId() {
        return taskId;
    }

    public int getPointsAwarded() {
        return pointsAwarded;
    }

    public TaskEvent getTaskEvent() {
        return taskEvent;
    }

    public JsonNode getMetadata() {
        return metadata;
    }
}
