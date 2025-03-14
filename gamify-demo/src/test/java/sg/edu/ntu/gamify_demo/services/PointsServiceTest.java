package sg.edu.ntu.gamify_demo.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sg.edu.ntu.gamify_demo.Services.LadderService;
import sg.edu.ntu.gamify_demo.Services.PointsService;
import sg.edu.ntu.gamify_demo.events.EventPublisher;
import sg.edu.ntu.gamify_demo.events.domain.DomainEventPublisher;
import sg.edu.ntu.gamify_demo.events.domain.PointsEarnedEvent;
import sg.edu.ntu.gamify_demo.events.domain.PointsSpentEvent;
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.models.PointsTransaction;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.repositories.PointsTransactionRepository;

/**
 * Unit tests for the PointsService class.
 * These tests focus on verifying the refactored event publishing functionality.
 */
public class PointsServiceTest {

    private PointsService pointsService;
    private UserService userService;
    private PointsTransactionRepository pointsTransactionRepository;
    private EventPublisher eventPublisher;
    private DomainEventPublisher domainEventPublisher;
    private ObjectMapper objectMapper;
    private LadderService ladderService;
    private User testUser;
    private JsonNode testMetadata;

    @BeforeEach
    public void setup() {
        // Create mocks
        userService = mock(UserService.class);
        pointsTransactionRepository = mock(PointsTransactionRepository.class);
        eventPublisher = mock(EventPublisher.class);
        domainEventPublisher = mock(DomainEventPublisher.class);
        objectMapper = new ObjectMapper();
        ladderService = mock(LadderService.class);

        // Create test user
        testUser = new User();
        testUser.setId("user123");
        testUser.setUsername("testuser");
        testUser.setEarnedPoints(100);
        testUser.setAvailablePoints(100);

        // Create test metadata
        ObjectNode metadata = objectMapper.createObjectNode();
        metadata.put("source", "test");
        testMetadata = metadata;

        // Configure mocks
        when(userService.getUserById("user123")).thenReturn(testUser);
        when(pointsTransactionRepository.save(any(PointsTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Create PointsService instance
        pointsService = new PointsService(
                userService,
                pointsTransactionRepository,
                eventPublisher,
                domainEventPublisher,
                objectMapper,
                ladderService);
    }

    @Test
    public void testAwardPoints_PublishesDomainEvent() {
        // Arrange
        int pointsToAward = 50;
        String source = "TEST_SOURCE";

        // Act
        int newPoints = pointsService.awardPoints("user123", pointsToAward, source, testMetadata);

        // Assert
        assertEquals(150, newPoints); // 100 + 50 = 150
        
        // Verify domain event was published
        ArgumentCaptor<PointsEarnedEvent> eventCaptor = ArgumentCaptor.forClass(PointsEarnedEvent.class);
        verify(domainEventPublisher, times(1)).publish(eventCaptor.capture());
        
        PointsEarnedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(testUser, capturedEvent.getUser());
        assertEquals(pointsToAward, capturedEvent.getPoints());
        assertEquals(150, capturedEvent.getNewTotal());
        assertEquals(source, capturedEvent.getSource());
        assertEquals(testMetadata, capturedEvent.getMetadata());
        
        // Verify user was updated
        verify(userService, times(1)).updateUser(eq("user123"), any(User.class));
        
        // Verify ladder status was updated
        verify(ladderService, times(1)).updateUserLadderStatus("user123");
        
        // Verify transaction was saved
        verify(pointsTransactionRepository, times(1)).save(any(PointsTransaction.class));
    }

    @Test
    public void testSpendPoints_PublishesDomainEvent() {
        // Arrange
        int pointsToSpend = 30;
        String source = "TEST_SPEND";

        // Act
        boolean result = pointsService.spendPoints("user123", pointsToSpend, source, testMetadata);

        // Assert
        assertTrue(result);
        
        // Verify domain event was published
        ArgumentCaptor<PointsSpentEvent> eventCaptor = ArgumentCaptor.forClass(PointsSpentEvent.class);
        verify(domainEventPublisher, times(1)).publish(eventCaptor.capture());
        
        PointsSpentEvent capturedEvent = eventCaptor.getValue();
        assertEquals(testUser, capturedEvent.getUser());
        assertEquals(pointsToSpend, capturedEvent.getPoints());
        assertEquals(70, capturedEvent.getNewTotal()); // 100 - 30 = 70
        assertEquals(source, capturedEvent.getSource());
        assertEquals(testMetadata, capturedEvent.getMetadata());
        
        // Verify user was updated
        verify(userService, times(1)).updateUser(eq("user123"), any(User.class));
        
        // Verify transaction was saved
        verify(pointsTransactionRepository, times(1)).save(any(PointsTransaction.class));
    }

    @Test
    public void testSpendPoints_InsufficientPoints() {
        // Arrange
        int pointsToSpend = 150; // More than available (100)
        String source = "TEST_SPEND";

        // Act
        boolean result = pointsService.spendPoints("user123", pointsToSpend, source, testMetadata);

        // Assert
        assertFalse(result);
        
        // Verify no domain event was published
        verify(domainEventPublisher, times(0)).publish(any());
        
        // Verify user was not updated
        verify(userService, times(0)).updateUser(anyString(), any(User.class));
        
        // Verify no transaction was saved
        verify(pointsTransactionRepository, times(0)).save(any(PointsTransaction.class));
    }
}
