package sg.edu.ntu.gamify_demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sg.edu.ntu.gamify_demo.models.TaskEvent;
import sg.edu.ntu.gamify_demo.models.User;

import java.util.List;

/**
 * Repository for TaskEvent entities.
 */
@Repository
public interface TaskEventRepository extends JpaRepository<TaskEvent, String> {
    
    /**
     * Find all task events for a specific user.
     * 
     * @param user The user to find events for.
     * @return A list of task events for the user.
     */
    List<TaskEvent> findByUser(User user);
    
    /**
     * Find all task events for a specific task.
     * 
     * @param taskId The ID of the task.
     * @return A list of task events for the task.
     */
    List<TaskEvent> findByTaskId(String taskId);
    
    /**
     * Find all task events of a specific type.
     * 
     * @param eventType The type of event.
     * @return A list of task events of the specified type.
     */
    List<TaskEvent> findByEventType(String eventType);
    
    /**
     * Find all task events for a specific user and task.
     * 
     * @param user The user to find events for.
     * @param taskId The ID of the task.
     * @return A list of task events for the user and task.
     */
    List<TaskEvent> findByUserAndTaskId(User user, String taskId);
}
