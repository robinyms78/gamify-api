package sg.edu.ntu.gamify_demo.events.domain.subscribers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import sg.edu.ntu.gamify_demo.events.domain.DomainEvent;
import sg.edu.ntu.gamify_demo.events.domain.DomainEventSubscriber;
import sg.edu.ntu.gamify_demo.events.domain.PointsEarnedEvent;
import sg.edu.ntu.gamify_demo.events.domain.PointsSpentEvent;

/**
 * Subscriber for points-related domain events.
 * This class demonstrates how to implement a subscriber for the new domain events.
 */
@Component
public class PointsEventSubscriber implements DomainEventSubscriber<DomainEvent> {
    
    private static final Logger logger = LoggerFactory.getLogger(PointsEventSubscriber.class);
    
    @Override
    public void onEvent(DomainEvent event) {
        if (event instanceof PointsEarnedEvent) {
            onPointsEarned((PointsEarnedEvent) event);
        } else if (event instanceof PointsSpentEvent) {
            onPointsSpent((PointsSpentEvent) event);
        }
    }
    
    @Override
    public boolean isInterestedIn(Class<? extends DomainEvent> eventType) {
        return PointsEarnedEvent.class.isAssignableFrom(eventType) || 
               PointsSpentEvent.class.isAssignableFrom(eventType);
    }
    
    /**
     * Handle points earned events.
     * 
     * @param event The points earned event.
     */
    private void onPointsEarned(PointsEarnedEvent event) {
        logger.info("User {} earned {} points from {}. New total: {}",
                event.getUser().getUsername(),
                event.getPoints(),
                event.getSource(),
                event.getNewTotal());
        
        // Additional business logic for points earned can be added here
        // For example:
        // - Check if the user has reached a points threshold for an achievement
        // - Update leaderboards
        // - Send notifications
    }
    
    /**
     * Handle points spent events.
     * 
     * @param event The points spent event.
     */
    private void onPointsSpent(PointsSpentEvent event) {
        logger.info("User {} spent {} points on {}. Remaining: {}",
                event.getUser().getUsername(),
                event.getPoints(),
                event.getSource(),
                event.getNewTotal());
        
        // Additional business logic for points spent can be added here
        // For example:
        // - Track spending patterns
        // - Update user statistics
        // - Send notifications
    }
}
