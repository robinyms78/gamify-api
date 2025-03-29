package sg.edu.ntu.gamify_demo.events;

import com.fasterxml.jackson.databind.JsonNode;
import sg.edu.ntu.gamify_demo.models.User;

/**
 * Interface for publishing events in the system.
 * This is part of the Observer pattern implementation.
 */
public interface EventPublisher {
    
    /**
     * Publishes an event to all registered listeners.
     * 
     * @param eventType The type of event.
     * @param user The user associated with the event.
     * @param eventData Additional data about the event.
     */
    void publishEvent(String eventType, User user, JsonNode eventData);
    
    /**
     * Registers a listener for events.
     * 
     * @param listener The listener to register.
     */
    void registerListener(EventListener listener);
    
    /**
     * Unregisters a listener.
     * 
     * @param listener The listener to unregister.
     */
    void unregisterListener(EventListener listener);
}
