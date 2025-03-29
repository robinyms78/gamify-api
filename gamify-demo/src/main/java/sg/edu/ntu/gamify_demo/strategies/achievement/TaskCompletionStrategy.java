package sg.edu.ntu.gamify_demo.strategies.achievement;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import sg.edu.ntu.gamify_demo.models.TaskEvent;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.enums.TaskStatus;
import sg.edu.ntu.gamify_demo.repositories.TaskEventRepository;

/**
 * Strategy implementation for evaluating task completion achievement criteria.
 * This strategy checks if a user has completed enough tasks to meet the threshold.
 */
@Component
public class TaskCompletionStrategy implements AchievementCriteriaStrategy {

    private final TaskEventRepository taskEventRepository;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param taskEventRepository Repository for task events.
     */
    //@Autowired
    public TaskCompletionStrategy(TaskEventRepository taskEventRepository) {
        this.taskEventRepository = taskEventRepository;
    }
    
    /**
     * Evaluates whether a user has completed enough tasks to meet the threshold.
     * 
     * @param user The user to evaluate.
     * @param criteria The criteria containing the task completion threshold.
     * @return true if the user's completed tasks meet or exceed the threshold, false otherwise.
     */
    @Override
    public boolean evaluate(User user, JsonNode criteria) {
        if (criteria == null || !criteria.has("count")) {
            return false;
        }
        
        int requiredCount = criteria.get("count").asInt();
        
        // Get all task events for the user
        List<TaskEvent> userTaskEvents = taskEventRepository.findByUser(user);
        
        // Filter for completed tasks
        List<TaskEvent> completedTasks = userTaskEvents.stream()
                .filter(event -> event.getStatus() == TaskStatus.COMPLETED)
                .collect(Collectors.toList());
        
        // Check if specific task type is required
        if (criteria.has("taskType")) {
            String taskType = criteria.get("taskType").asText();
            completedTasks = completedTasks.stream()
                    .filter(event -> {
                        JsonNode metadata = event.getMetadata();
                        return metadata != null && 
                               metadata.has("taskType") && 
                               metadata.get("taskType").asText().equals(taskType);
                    })
                    .collect(Collectors.toList());
        }
        
        return completedTasks.size() >= requiredCount;
    }
}
