# Points Event Refactoring

## Overview

This document describes the refactoring of the points-related event handling in the gamify-demo application. The refactoring introduces explicit domain event classes for points-related events, improving type safety, maintainability, and extensibility.

## Changes Made

1. **Created Domain Event Classes**:
   - `PointsEarnedEvent`: Represents a user earning points.
   - `PointsSpentEvent`: Represents a user spending points.

2. **Updated `PointsService`**:
   - Modified `awardPoints` and `spendPoints` methods to create and publish domain events.
   - Added `DomainEventPublisher` dependency for publishing domain events.

3. **Updated `DomainEventPublisher`**:
   - Added handling for `PointsEarnedEvent` and `PointsSpentEvent` when forwarding to the legacy event system.

4. **Created `PointsEventSubscriber`**:
   - Implemented a subscriber for points-related domain events.
   - Added logging for points earned and spent events.
   - Provided placeholders for additional business logic.

5. **Updated `EventConfig`**:
   - Registered `PointsEventSubscriber` with the `DomainEventPublisher`.

## Benefits

1. **Type Safety**: Using explicit event classes instead of string-based event types reduces the risk of errors.
2. **Maintainability**: Event data is now encapsulated in dedicated classes, making it easier to understand and modify.
3. **Extensibility**: New event types can be added by creating new domain event classes and subscribers.
4. **Testability**: Domain events and subscribers can be tested in isolation.

## Usage

### Publishing Events

The `PointsService` now publishes domain events automatically when points are awarded or spent. No additional code is needed to publish these events.

```java
// Example of awarding points (this will automatically publish a PointsEarnedEvent)
pointsService.awardPoints(userId, 100, "TASK_COMPLETED", metadata);

// Example of spending points (this will automatically publish a PointsSpentEvent)
pointsService.spendPoints(userId, 50, "REWARD_REDEMPTION", metadata);
```

### Subscribing to Events

To subscribe to points-related events, create a class that implements `DomainEventSubscriber<DomainEvent>` and register it with the `DomainEventPublisher`. The `PointsEventSubscriber` class provides an example of how to do this.

```java
@Component
public class CustomPointsSubscriber implements DomainEventSubscriber<DomainEvent> {
    
    @Override
    public void onEvent(DomainEvent event) {
        if (event instanceof PointsEarnedEvent) {
            // Handle points earned event
        } else if (event instanceof PointsSpentEvent) {
            // Handle points spent event
        }
    }
    
    @Override
    public boolean isInterestedIn(Class<? extends DomainEvent> eventType) {
        return PointsEarnedEvent.class.isAssignableFrom(eventType) || 
               PointsSpentEvent.class.isAssignableFrom(eventType);
    }
}
```

Then register the subscriber in `EventConfig`:

```java
@Configuration
public class EventConfig {
    
    @Autowired
    public EventConfig(
            DomainEventPublisher domainEventPublisher,
            CustomPointsSubscriber customPointsSubscriber) {
        // ...
        this.customPointsSubscriber = customPointsSubscriber;
    }
    
    @PostConstruct
    public void registerSubscribers() {
        // ...
        domainEventPublisher.register(customPointsSubscriber);
    }
}
```

## Future Improvements

1. **Asynchronous Event Processing**: Consider making event processing asynchronous to improve performance.
2. **Event Persistence**: Consider persisting events for audit and replay purposes.
3. **Event Versioning**: Implement versioning for events to handle schema changes.
4. **More Specific Subscribers**: Create subscribers that handle specific event types rather than generic subscribers.
