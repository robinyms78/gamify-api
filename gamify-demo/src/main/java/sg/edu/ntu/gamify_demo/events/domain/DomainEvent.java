package sg.edu.ntu.gamify_demo.events.domain;

import java.time.LocalDateTime;

import sg.edu.ntu.gamify_demo.models.User;

/**
 * Base class for domain events in the system.
 * This follows the Domain Events pattern to represent significant occurrences in the domain.
 */
public abstract class DomainEvent {
    
    private final String eventType;
    private final User user;
    private final LocalDateTime timestamp;
    
    /**
     * Constructor for a domain event.
     * 
     * @param eventType The type of event.
     * @param user The user associated with the event.
     */
    protected DomainEvent(String eventType, User user) {
        this.eventType = eventType;
        this.user = user;
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Get the type of event.
     * 
     * @return The event type.
     */
    public String getEventType() {
        return eventType;
    }
    
    /**
     * Get the user associated with the event.
     * 
     * @return The user.
     */
    public User getUser() {
        return user;
    }
    
    /**
     * Get the timestamp of the event.
     * 
     * @return The timestamp.
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
