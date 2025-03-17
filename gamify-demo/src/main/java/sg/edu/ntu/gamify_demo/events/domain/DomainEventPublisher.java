package sg.edu.ntu.gamify_demo.events.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.Arrays;

import sg.edu.ntu.gamify_demo.events.EventPublisher;


/**
 * Publisher for domain events.
 * This follows the Observer pattern to publish domain events to interested listeners.
 */
@Component
public class DomainEventPublisher {
    
    private final List<DomainEventSubscriber<?>> subscribers = new CopyOnWriteArrayList<>();
    private final EventPublisher legacyEventPublisher;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    
    /**
     * Constructor for dependency injection.
     */
    //@Autowired
    public DomainEventPublisher(EventPublisher legacyEventPublisher, ObjectMapper objectMapper, MeterRegistry meterRegistry) {
        this.legacyEventPublisher = legacyEventPublisher;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
    }
    
    /**
     * Publish a domain event to all interested subscribers.
     * Also publishes to the legacy event system for backward compatibility.
     * 
     * @param <T> The type of domain event.
     * @param event The domain event to publish.
     */
    @SuppressWarnings("unchecked")
    public <T extends DomainEvent> void publish(T event) {
        // Record metrics
        if (meterRegistry != null) {
            List<Tag> tags = Arrays.asList(
                Tag.of("event_type", event.getEventType()),
                Tag.of("user", event.getUser().getId())
            );
            meterRegistry.counter("events.processed", tags).increment();
        }
        
        // Publish to domain event subscribers
        for (DomainEventSubscriber<?> subscriber : getInterestedSubscribers(event)) {
            ((DomainEventSubscriber<T>) subscriber).onEvent(event);
        }
        
        // Publish to legacy event system for backward compatibility
        if (legacyEventPublisher != null) {
            // Convert domain event to legacy format
            ObjectNode legacyEventData = objectMapper.createObjectNode();
            
            if (event instanceof TaskCompletedEvent) {
                TaskCompletedEvent taskEvent = (TaskCompletedEvent) event;
                legacyEventData.put("taskId", taskEvent.getTaskId());
                legacyEventData.put("eventId", taskEvent.getTaskEvent().getEventId());
                legacyEventData.put("pointsAwarded", taskEvent.getPointsAwarded());
                
                if (taskEvent.getMetadata() != null) {
                    legacyEventData.set("metadata", taskEvent.getMetadata());
                }
            } else if (event instanceof PointsEarnedEvent) {
                PointsEarnedEvent pointsEvent = (PointsEarnedEvent) event;
                legacyEventData.put("points", pointsEvent.getPoints());
                legacyEventData.put("newTotal", pointsEvent.getNewTotal());
                legacyEventData.put("source", pointsEvent.getSource());
                
                if (pointsEvent.getMetadata() != null) {
                    legacyEventData.set("metadata", pointsEvent.getMetadata());
                }
            } else if (event instanceof PointsSpentEvent) {
                PointsSpentEvent pointsEvent = (PointsSpentEvent) event;
                legacyEventData.put("points", pointsEvent.getPoints());
                legacyEventData.put("newTotal", pointsEvent.getNewTotal());
                legacyEventData.put("source", pointsEvent.getSource());
                
                if (pointsEvent.getMetadata() != null) {
                    legacyEventData.set("metadata", pointsEvent.getMetadata());
                }
            }
            
            legacyEventPublisher.publishEvent(event.getEventType(), event.getUser(), legacyEventData);
        }
    }
    
    /**
     * Register a subscriber for domain events.
     * 
     * @param <T> The type of domain event.
     * @param subscriber The subscriber to register.
     */
    public <T extends DomainEvent> void register(DomainEventSubscriber<T> subscriber) {
        if (!subscribers.contains(subscriber)) {
            subscribers.add(subscriber);
        }
    }
    
    /**
     * Unregister a subscriber.
     * 
     * @param <T> The type of domain event.
     * @param subscriber The subscriber to unregister.
     */
    public <T extends DomainEvent> void unregister(DomainEventSubscriber<T> subscriber) {
        subscribers.remove(subscriber);
    }
    
    /**
     * Get all subscribers interested in a specific domain event.
     * 
     * @param <T> The type of domain event.
     * @param event The domain event.
     * @return A list of interested subscribers.
     */
    @SuppressWarnings("unchecked")
    private <T extends DomainEvent> List<DomainEventSubscriber<T>> getInterestedSubscribers(T event) {
        List<DomainEventSubscriber<T>> interestedSubscribers = new ArrayList<>();
        
        for (DomainEventSubscriber<?> subscriber : subscribers) {
            if (subscriber.isInterestedIn(event.getClass())) {
                interestedSubscribers.add((DomainEventSubscriber<T>) subscriber);
            }
        }
        
        return interestedSubscribers;
    }
}
