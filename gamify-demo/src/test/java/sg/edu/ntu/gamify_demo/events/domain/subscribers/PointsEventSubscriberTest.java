package sg.edu.ntu.gamify_demo.events.domain.subscribers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sg.edu.ntu.gamify_demo.events.domain.DomainEvent;
import sg.edu.ntu.gamify_demo.events.domain.PointsEarnedEvent;
import sg.edu.ntu.gamify_demo.events.domain.PointsSpentEvent;
import sg.edu.ntu.gamify_demo.events.domain.TaskCompletedEvent;
import sg.edu.ntu.gamify_demo.models.TaskEvent;
import sg.edu.ntu.gamify_demo.models.User;

/**
 * Unit tests for the PointsEventSubscriber class.
 * These tests focus on verifying that the subscriber correctly handles points-related events.
 */
public class PointsEventSubscriberTest {

    private PointsEventSubscriber subscriber;
    private User testUser;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        // Create subscriber
        subscriber = new PointsEventSubscriber();
        
        // Set logger field using reflection (for testing purposes)
        try {
            java.lang.reflect.Field loggerField = PointsEventSubscriber.class.getDeclaredField("logger");
            loggerField.setAccessible(true);
            loggerField.set(subscriber, mock(Logger.class));
        } catch (Exception e) {
            // If this fails, the tests will still run, but without mocking the logger
        }
        
        // Create test user
        testUser = new User();
        testUser.setId("user123");
        testUser.setUsername("testuser");
        testUser.setEarnedPoints(100);
        testUser.setAvailablePoints(100);
        
        // Create object mapper
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testIsInterestedIn_PointsEarnedEvent() {
        // Assert
        assertTrue(subscriber.isInterestedIn(PointsEarnedEvent.class));
    }

    @Test
    public void testIsInterestedIn_PointsSpentEvent() {
        // Assert
        assertTrue(subscriber.isInterestedIn(PointsSpentEvent.class));
    }

    @Test
    public void testIsInterestedIn_OtherEvent() {
        // Assert
        assertFalse(subscriber.isInterestedIn(TaskCompletedEvent.class));
    }

    @Test
    public void testOnEvent_PointsEarnedEvent() {
        // Arrange
        ObjectNode metadata = objectMapper.createObjectNode();
        metadata.put("source", "test");
        
        PointsEarnedEvent event = new PointsEarnedEvent(
                testUser,
                50,
                150,
                "TEST_SOURCE",
                metadata);
        
        // Act - This will log the event, which we've mocked
        subscriber.onEvent(event);
        
        // No assertions needed as we're just testing that the method doesn't throw exceptions
        // In a real test, we might verify that the logger was called with the expected message
    }

    @Test
    public void testOnEvent_PointsSpentEvent() {
        // Arrange
        ObjectNode metadata = objectMapper.createObjectNode();
        metadata.put("source", "test");
        
        PointsSpentEvent event = new PointsSpentEvent(
                testUser,
                30,
                70,
                "TEST_SPEND",
                metadata);
        
        // Act - This will log the event, which we've mocked
        subscriber.onEvent(event);
        
        // No assertions needed as we're just testing that the method doesn't throw exceptions
        // In a real test, we might verify that the logger was called with the expected message
    }

    @Test
    public void testOnEvent_OtherEvent() {
        // Arrange
        TaskEvent taskEvent = new TaskEvent();
        taskEvent.setEventId("event123");
        taskEvent.setTaskId("task123");
        
        TaskCompletedEvent event = new TaskCompletedEvent(
                "TASK_COMPLETED",
                testUser,
                taskEvent,
                50,
                null);
        
        // Act - This should be a no-op for this subscriber
        subscriber.onEvent(event);
        
        // No assertions needed as we're just testing that the method doesn't throw exceptions
    }
}
