package sg.edu.ntu.gamify_demo.events.domain.subscribers;

import org.springframework.stereotype.Component;
import sg.edu.ntu.gamify_demo.events.domain.DomainEvent;
import sg.edu.ntu.gamify_demo.events.domain.DomainEventSubscriber;
import sg.edu.ntu.gamify_demo.events.domain.PointsEarnedEvent;
import sg.edu.ntu.gamify_demo.interfaces.LadderStatusService;
import sg.edu.ntu.gamify_demo.models.User;

/**
 * Subscriber for PointsEarnedEvent to update user ladder status.
 * This follows the Observer pattern to react to points being earned.
 */
@Component
public class LadderStatusSubscriber implements DomainEventSubscriber<PointsEarnedEvent> {
    
    private final LadderStatusService ladderStatusService;
    
    /**
     * Constructor for dependency injection.
     */
    public LadderStatusSubscriber(LadderStatusService ladderStatusService) {
        this.ladderStatusService = ladderStatusService;
    }
    
    /**
     * Handle a PointsEarnedEvent by updating the user's ladder status.
     * 
     * @param event The PointsEarnedEvent.
     */
    @Override
    public void onEvent(PointsEarnedEvent event) {
        User user = event.getUser();
        ladderStatusService.updateUserLadderStatus(user.getId());
    }
    
    /**
     * Check if this subscriber is interested in a specific type of domain event.
     * 
     * @param eventType The class of the domain event.
     * @return True if this subscriber is interested in PointsEarnedEvent, false otherwise.
     */
    @Override
    public boolean isInterestedIn(Class<? extends DomainEvent> eventType) {
        return eventType == PointsEarnedEvent.class;
    }
}
