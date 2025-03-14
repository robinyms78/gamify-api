package sg.edu.ntu.gamify_demo.events.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sg.edu.ntu.gamify_demo.events.EventPublisher;
import sg.edu.ntu.gamify_demo.models.TaskEvent;
import sg.edu.ntu.gamify_demo.models.User;

/**
 * Unit tests for the DomainEventPublisher class.
 * These tests focus on verifying that the publisher correctly handles different types of domain events.
 */
public class DomainEventPublisherTest {

    private DomainEventPublisher publisher;
    private EventPublisher legacyEventPublisher;
    private ObjectMapper objectMapper;
    private User testUser;
    private DomainEventSubscriber<DomainEvent> testSubscriber;
    private MeterRegistry meterRegistry;
    private Counter eventsProcessedCounter;
    private Timer eventProcessingTimer;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setup() {
        // Create mocks
        legacyEventPublisher = mock(EventPublisher.class);
        objectMapper = new ObjectMapper();
        testSubscriber = mock(DomainEventSubscriber.class);
        meterRegistry = mock(MeterRegistry.class);
        eventsProcessedCounter = mock(Counter.class);
        eventProcessingTimer = mock(Timer.class);
        
        when(meterRegistry.counter(anyString(), any(Iterable.class))).thenReturn(eventsProcessedCounter);
        when(meterRegistry.timer(anyString())).thenReturn(eventProcessingTimer);
        
        // Configure subscriber mock
        when(testSubscriber.isInterestedIn(any())).thenReturn(true);
        
        // Create publisher
        publisher = new DomainEventPublisher(legacyEventPublisher, objectMapper, meterRegistry);
        
        // Register subscriber
        publisher.register(testSubscriber);
        
        // Create test user
        testUser = new User();
        testUser.setId("user123");
        testUser.setUsername("testuser");
        testUser.setEarnedPoints(100L);
        testUser.setAvailablePoints(100L);
    }

    @Test
    public void testPublish_TaskCompletedEvent() {
        // Arrange
        TaskEvent taskEvent = new TaskEvent();
        taskEvent.setEventId("event123");
        taskEvent.setTaskId("task123");
        
        ObjectNode metadata = objectMapper.createObjectNode();
        metadata.put("priority", "HIGH");
        
        TaskCompletedEvent event = new TaskCompletedEvent(
                "TASK_COMPLETED",
                testUser,
                taskEvent,
                30,
                metadata);
        
        // Act
        publisher.publish(event);
        
        // Assert
        // Verify subscriber was notified
        verify(testSubscriber, times(1)).onEvent(event);
        
        // Verify metrics
        ArgumentCaptor<Iterable<Tag>> tagsCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(meterRegistry).counter(eq("events.processed"), tagsCaptor.capture());
        
        verify(eventsProcessedCounter).increment();
        
        // Verify legacy event publisher was called with correct parameters
        ArgumentCaptor<JsonNode> dataCaptor = ArgumentCaptor.forClass(JsonNode.class);
        verify(legacyEventPublisher, times(1)).publishEvent(
                eq(event.getEventType()),
                eq(testUser),
                dataCaptor.capture());
        
        JsonNode capturedData = dataCaptor.getValue();
        assertEquals("task123", capturedData.get("taskId").asText());
        assertEquals("event123", capturedData.get("eventId").asText());
        assertEquals(30, capturedData.get("pointsAwarded").asInt());
        assertEquals("HIGH", capturedData.get("metadata").get("priority").asText());
    }

    @Test
    public void testPublish_PointsEarnedEvent() {
        // Arrange
        ObjectNode metadata = objectMapper.createObjectNode();
        metadata.put("source", "test");
        
        PointsEarnedEvent event = new PointsEarnedEvent(
                testUser,
                50,
                150,
                "TEST_SOURCE",
                metadata);
        
        // Act
        publisher.publish(event);
        
        // Assert
        // Verify subscriber was notified
        verify(testSubscriber, times(1)).onEvent(event);
        
        // Verify legacy event publisher was called with correct parameters
        ArgumentCaptor<JsonNode> dataCaptor = ArgumentCaptor.forClass(JsonNode.class);
        verify(legacyEventPublisher, times(1)).publishEvent(
                eq(event.getEventType()),
                eq(testUser),
                dataCaptor.capture());
        
        JsonNode capturedData = dataCaptor.getValue();
        assertEquals(50, capturedData.get("points").asInt());
        assertEquals(150, capturedData.get("newTotal").asInt());
        assertEquals("TEST_SOURCE", capturedData.get("source").asText());
        assertEquals("test", capturedData.get("metadata").get("source").asText());
    }

    @Test
    public void testPublish_PointsSpentEvent() {
        // Arrange
        ObjectNode metadata = objectMapper.createObjectNode();
        metadata.put("source", "test");
        
        PointsSpentEvent event = new PointsSpentEvent(
                testUser,
                30,
                70,
                "TEST_SPEND",
                metadata);
        
        // Act
        publisher.publish(event);
        
        // Assert
        // Verify subscriber was notified
        verify(testSubscriber, times(1)).onEvent(event);
        
        // Verify legacy event publisher was called with correct parameters
        ArgumentCaptor<JsonNode> dataCaptor = ArgumentCaptor.forClass(JsonNode.class);
        verify(legacyEventPublisher, times(1)).publishEvent(
                eq(event.getEventType()),
                eq(testUser),
                dataCaptor.capture());
        
        JsonNode capturedData = dataCaptor.getValue();
        assertEquals(30, capturedData.get("points").asInt());
        assertEquals(70, capturedData.get("newTotal").asInt());
        assertEquals("TEST_SPEND", capturedData.get("source").asText());
        assertEquals("test", capturedData.get("metadata").get("source").asText());
    }
    
    private void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }
}
