package sg.edu.ntu.gamify_demo.models;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sg.edu.ntu.gamify_demo.models.enums.TaskStatus;

/**
 * The TaskEvent class represents events related to tasks assigned to users.
 * It tracks task assignments, updates, and completions with relevant timestamps.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "task_events")
public class TaskEvent {
    @Id
    @Column(name = "event_id")
    private String eventId;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "task_id", nullable = false)
    private String taskId;
    
    @Column(name = "event_type", nullable = false)
    private String eventType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskStatus status;
    
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    @Column(name = "completion_time")
    private LocalDateTime completionTime;
    
    @Type(JsonType.class)
    @Column(name = "metadata", columnDefinition = "json")
    private JsonNode metadata;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Constructs a TaskEvent for a task assignment.
     * 
     * @param user The user the task is assigned to.
     * @param taskId The unique identifier for the task.
     * @param dueDate The due date for the task (optional).
     * @param metadata Additional data about the task.
     * @return A new TaskEvent representing a task assignment.
     */
    public static TaskEvent createAssignmentEvent(User user, String taskId, LocalDateTime dueDate, JsonNode metadata) {
        TaskEvent event = new TaskEvent();
        event.eventId = UUID.randomUUID().toString();
        event.user = user;
        event.taskId = taskId;
        event.eventType = "TASK_ASSIGNED";
        event.status = TaskStatus.ASSIGNED;
        event.assignedAt = LocalDateTime.now();
        event.dueDate = dueDate;
        event.metadata = metadata;
        return event;
    }
    
    /**
     * Updates the status of this task to in progress.
     */
    public void markInProgress() {
        this.status = TaskStatus.IN_PROGRESS;
        this.eventType = "TASK_UPDATED";
    }
    
    /**
     * Marks this task as completed with the current timestamp.
     */
    public void markCompleted() {
        this.status = TaskStatus.COMPLETED;
        this.eventType = "TASK_COMPLETED";
        this.completionTime = LocalDateTime.now();
    }
    
    /**
     * Marks this task as cancelled.
     */
    public void markCancelled() {
        this.status = TaskStatus.CANCELLED;
        this.eventType = "TASK_CANCELLED";
    }
}
