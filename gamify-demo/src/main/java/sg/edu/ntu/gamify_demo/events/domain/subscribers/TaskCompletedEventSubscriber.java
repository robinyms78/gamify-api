package sg.edu.ntu.gamify_demo.events.domain.subscribers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import sg.edu.ntu.gamify_demo.events.domain.DomainEvent;
import sg.edu.ntu.gamify_demo.events.domain.DomainEventSubscriber;
import sg.edu.ntu.gamify_demo.events.domain.TaskCompletedEvent;
import sg.edu.ntu.gamify_demo.services.PointsService;

/**
 * Subscriber for task completion events.
 * This follows the Observer pattern to receive notifications about task completions.
 */
@Component
public class TaskCompletedEventSubscriber implements DomainEventSubscriber<TaskCompletedEvent> {
    
    private static final Logger logger = LoggerFactory.getLogger(TaskCompletedEventSubscriber.class);
    
    private final PointsService pointsService;
    
    /**
     * Constructor for dependency injection.
     */
    @Autowired
    public TaskCompletedEventSubscriber(PointsService pointsService) {
        this.pointsService = pointsService;
    }
    
    @Override
    public void onEvent(TaskCompletedEvent event) {
        logger.info("Task completed event received: User {} completed task {} and earned {} points",
                event.getUser().getUsername(),
                event.getTaskId(),
                event.getPointsAwarded());
        
        // Additional processing can be done here
        // For example, we could update statistics, trigger notifications, etc.
    }
    
    @Override
    public boolean isInterestedIn(Class<? extends DomainEvent> eventType) {
        return TaskCompletedEvent.class.equals(eventType);
    }
}
