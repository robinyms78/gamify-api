package sg.edu.ntu.gamify_demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import sg.edu.ntu.gamify_demo.events.domain.DomainEventPublisher;
import sg.edu.ntu.gamify_demo.events.domain.subscribers.TaskCompletedEventSubscriber;

/**
 * Configuration for event-related components.
 * This class registers event subscribers with the appropriate publishers.
 */
@Configuration
public class EventConfig {
    
    private final DomainEventPublisher domainEventPublisher;
    private final TaskCompletedEventSubscriber taskCompletedEventSubscriber;
    
    /**
     * Constructor for dependency injection.
     */
    @Autowired
    public EventConfig(
            DomainEventPublisher domainEventPublisher,
            TaskCompletedEventSubscriber taskCompletedEventSubscriber) {
        this.domainEventPublisher = domainEventPublisher;
        this.taskCompletedEventSubscriber = taskCompletedEventSubscriber;
    }
    
    /**
     * Register event subscribers with their respective publishers.
     * This method is called after the bean is constructed.
     */
    @PostConstruct
    public void registerSubscribers() {
        // Register domain event subscribers
        domainEventPublisher.register(taskCompletedEventSubscriber);
    }
}
