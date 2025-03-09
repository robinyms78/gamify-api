package sg.edu.ntu.gamify_demo.events.domain;

/**
 * Interface for subscribing to domain events.
 * This follows the Observer pattern to receive notifications about domain events.
 * 
 * @param <T> The type of domain event this subscriber is interested in.
 */
public interface DomainEventSubscriber<T extends DomainEvent> {
    
    /**
     * Called when a domain event is published.
     * 
     * @param event The domain event.
     */
    void onEvent(T event);
    
    /**
     * Check if this subscriber is interested in a specific type of domain event.
     * 
     * @param eventType The class of the domain event.
     * @return True if this subscriber is interested in the event type, false otherwise.
     */
    boolean isInterestedIn(Class<? extends DomainEvent> eventType);
}
