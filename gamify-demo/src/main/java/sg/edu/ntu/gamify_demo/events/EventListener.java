package sg.edu.ntu.gamify_demo.events;

import com.fasterxml.jackson.databind.JsonNode;
import sg.edu.ntu.gamify_demo.models.User;

/**
 * Interface for listening to events in the system.
 * This is part of the Observer pattern implementation.
 */
public interface EventListener {
    
    /**
     * Called when an event is published.
     * 
     * @param eventType The type of event.
     * @param user The user associated with the event.
     * @param eventData Additional data about the event.
     */
    void onEvent(String eventType, User user, JsonNode eventData);
    
    /**
     * Returns the types of events this listener is interested in.
     * 
     * @return An array of event types.
     */
    String[] getInterestedEventTypes();
}
