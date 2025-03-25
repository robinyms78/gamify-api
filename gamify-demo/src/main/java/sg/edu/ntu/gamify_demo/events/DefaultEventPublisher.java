package sg.edu.ntu.gamify_demo.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import sg.edu.ntu.gamify_demo.models.User;

/**
 * Default implementation of the EventPublisher interface.
 * This class manages event listeners and publishes events to interested listeners.
 */
@Component
public class DefaultEventPublisher implements EventPublisher {

    private final List<EventListener> listeners = new CopyOnWriteArrayList<>();
    
    @Override
    public void publishEvent(String eventType, User user, JsonNode eventData) {
        for (EventListener listener : getInterestedListeners(eventType)) {
            listener.onEvent(eventType, user, eventData);
        }
    }

    @Override
    public void registerListener(EventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void unregisterListener(EventListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Gets all listeners interested in a specific event type.
     * 
     * @param eventType The event type.
     * @return A list of interested listeners.
     */
    private List<EventListener> getInterestedListeners(String eventType) {
        List<EventListener> interestedListeners = new ArrayList<>();
        
        for (EventListener listener : listeners) {
            String[] interestedEventTypes = listener.getInterestedEventTypes();
            
            if (interestedEventTypes == null) {
                continue;
            }
            
            if (Arrays.asList(interestedEventTypes).contains(eventType)) {
                interestedListeners.add(listener);
            }
        }
        
        return interestedListeners;
    }
}
