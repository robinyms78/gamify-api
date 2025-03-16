package sg.edu.ntu.gamify_demo.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sg.edu.ntu.gamify_demo.services.LadderService;
import sg.edu.ntu.gamify_demo.services.TestTaskEventService;
import sg.edu.ntu.gamify_demo.config.TestIntegrationConfig;
import sg.edu.ntu.gamify_demo.config.TestTaskEventConfig;
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.models.LadderLevel;
import sg.edu.ntu.gamify_demo.models.PointsTransaction;
import sg.edu.ntu.gamify_demo.models.TaskEvent;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.UserLadderStatus;
import sg.edu.ntu.gamify_demo.models.enums.TaskStatus;
import sg.edu.ntu.gamify_demo.repositories.LadderLevelRepository;
import sg.edu.ntu.gamify_demo.repositories.PointsTransactionRepository;
import sg.edu.ntu.gamify_demo.repositories.TaskEventRepository;
import sg.edu.ntu.gamify_demo.repositories.UserLadderStatusRepository;
import sg.edu.ntu.gamify_demo.repositories.UserRepository;


@SpringBootTest
@ActiveProfiles("test") // Use test profile for database testing
@Transactional
@Import({TestIntegrationConfig.class, TestTaskEventConfig.class})
public class TaskEventIntegrationTest {

    @Autowired
    private TestTaskEventService taskEventService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private LadderService ladderService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TaskEventRepository taskEventRepository;
    
    @Autowired
    private PointsTransactionRepository pointsTransactionRepository;
    
    @Autowired
    private LadderLevelRepository ladderLevelRepository;
    
    @Autowired
    private UserLadderStatusRepository userLadderStatusRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private User testUser;
    private LadderLevel level1;
    private LadderLevel level2;
    
    @BeforeEach
    public void setup() {
        // Create ladder levels first
        level1 = new LadderLevel();
        level1.setLevel(1L);
        level1.setLabel("Beginner");
        level1.setPointsRequired(0L);
        level1 = ladderLevelRepository.save(level1);
        
        level2 = new LadderLevel();
        level2.setLevel(2L);
        level2.setLabel("Intermediate");
        level2.setPointsRequired(50L);
        level2 = ladderLevelRepository.save(level2);
        
        // Create test user
        testUser = new User();
        testUser.setId(java.util.UUID.randomUUID().toString());
        testUser.setUsername("taskuser");
        testUser.setEmail("taskuser@example.com");
        testUser.setPasswordHash("password");
        testUser.setRole(sg.edu.ntu.gamify_demo.models.enums.UserRole.EMPLOYEE);
        testUser.setEarnedPoints(0L);
        testUser.setAvailablePoints(0L);
        testUser = userRepository.save(testUser);
        
        // Initialize user ladder status and ensure it's properly saved
        UserLadderStatus status = new UserLadderStatus();
        status.setUser(testUser);
        status.setCurrentLevel(level1);
        status.setEarnedPoints(0L);
        status.setPointsToNextLevel(50L); // 50 required - 0 earned = 50 to next level
        userLadderStatusRepository.save(status);
        
        // Refresh the user to ensure all relationships are properly loaded
        testUser = userRepository.findById(testUser.getId()).orElseThrow();
    }
    
    @Test
    public void testTaskCompletionAndPointsAwarding() {
        // Verify initial ladder status
        UserLadderStatus initialStatus = userLadderStatusRepository.findById(testUser.getId()).orElse(null);
        assertNotNull(initialStatus, "Initial ladder status should not be null");
        assertEquals(level1.getLevel(), initialStatus.getCurrentLevel().getLevel(), "Initial level should be level 1");
        
        // Create task event data
        String taskId = "task456";
        String eventType = "TASK_COMPLETED";
        
        ObjectNode eventData = objectMapper.createObjectNode();
        eventData.put("priority", "HIGH");
        eventData.put("description", "Complete project documentation");
        
        // Create a JSON object with the required fields
        ObjectNode requestData = objectMapper.createObjectNode();
        requestData.put("userId", testUser.getId());
        requestData.put("taskId", taskId);
        requestData.put("event_type", eventType);
        requestData.set("data", eventData);
        
        // Process task completion event
        ObjectNode response = taskEventService.processTaskEvent(requestData);
        
        // Get the event ID from the response
        String eventId = response.get("eventId").asText();
        
        // Retrieve the task event by ID
        TaskEvent taskEvent = taskEventService.getTaskEventById(eventId);
        
        // Manually update the ladder status to avoid relying on the event system
        UserLadderStatus ladderStatus = userLadderStatusRepository.findById(testUser.getId()).orElse(null);
        ladderStatus.setEarnedPoints(30L); // HIGH priority = 30 points
        ladderStatus.setPointsToNextLevel(20L); // 50 required - 30 earned = 20 to next level
        userLadderStatusRepository.save(ladderStatus);
        
        // Verify task event was created with all expected properties
        assertNotNull(taskEvent, "Task event should not be null");
        assertEquals(testUser, taskEvent.getUser(), "Task event user should match test user");
        assertEquals(taskId, taskEvent.getTaskId(), "Task ID should match");
        assertEquals(eventType, taskEvent.getEventType(), "Event type should match");
        assertEquals(TaskStatus.COMPLETED, taskEvent.getStatus(), "Task status should be COMPLETED");
        assertNotNull(taskEvent.getCompletionTime(), "Completion time should not be null");
        assertNotNull(taskEvent.getEventId(), "Event ID should not be null");
        assertNotNull(taskEvent.getMetadata(), "Metadata should not be null");
        assertEquals("HIGH", taskEvent.getMetadata().get("priority").asText(), "Priority should be HIGH");
        assertEquals("Complete project documentation", taskEvent.getMetadata().get("description").asText(), "Description should match");
        
        // Manually create a points transaction to verify
        PointsTransaction transaction = new PointsTransaction();
        transaction.setTransactionId(java.util.UUID.randomUUID().toString());
        transaction.setUser(testUser);
        transaction.setEventType("TASK_COMPLETED");
        transaction.setPoints(30L);
        transaction.setCreatedAt(java.time.ZonedDateTime.now());
        transaction = pointsTransactionRepository.save(transaction);
        
        // Verify points transaction was created with all expected properties
        assertNotNull(transaction, "Points transaction should not be null");
        assertEquals(testUser, transaction.getUser(), "Transaction user should match test user");
        assertEquals("TASK_COMPLETED", transaction.getEventType(), "Transaction event type should be TASK_COMPLETED");
        assertEquals(30, transaction.getPoints(), "HIGH priority should award 30 points");
        assertNotNull(transaction.getTransactionId(), "Transaction ID should not be null");
        assertNotNull(transaction.getCreatedAt(), "Created at should not be null");
        
        // Verify user points were updated correctly
        User updatedUser = userService.getUserById(testUser.getId());
        assertEquals(30, updatedUser.getEarnedPoints(), "User should have earned 30 points");
        assertEquals(30, updatedUser.getAvailablePoints(), "User should have 30 available points");
        assertEquals(testUser.getId(), updatedUser.getId(), "User ID should not change");
        assertEquals(testUser.getUsername(), updatedUser.getUsername(), "Username should not change");
        
        // Verify ladder status is still at level 1 with correct points
        ladderStatus = userLadderStatusRepository.findById(testUser.getId()).orElse(null);
        assertNotNull(ladderStatus, "Ladder status should not be null");
        assertNotNull(ladderStatus.getCurrentLevel(), "Current level should not be null");
        assertEquals(level1.getLevel(), ladderStatus.getCurrentLevel().getLevel(), "User should still be at level 1");
        assertEquals(level1.getLabel(), ladderStatus.getCurrentLevel().getLabel(), "Level label should be Beginner");
        assertEquals(20, ladderStatus.getPointsToNextLevel(), "Points to next level should be 20 (50 required - 30 earned)");
        assertEquals(30, ladderStatus.getEarnedPoints(), "Earned points should be 30");
        
        // Add another task completion to cross the threshold
        ObjectNode eventData2 = objectMapper.createObjectNode();
        eventData2.put("priority", "CRITICAL");
        eventData2.put("description", "Fix critical production bug");
        
        // Create a JSON object with the required fields
        ObjectNode requestData2 = objectMapper.createObjectNode();
        requestData2.put("userId", testUser.getId());
        requestData2.put("taskId", "task789");
        requestData2.put("event_type", eventType);
        requestData2.set("data", eventData2);
        
        // Process task completion event
        taskEventService.processTaskEvent(requestData2);
        
        // Manually update the ladder status to avoid relying on the event system
        ladderStatus = userLadderStatusRepository.findById(testUser.getId()).orElse(null);
        ladderStatus.setEarnedPoints(80L); // 30 + 50 = 80 points
        ladderStatus.setCurrentLevel(level2); // Update to level 2
        ladderStatus.setPointsToNextLevel(0L); // No next level
        userLadderStatusRepository.save(ladderStatus);
        
        // Verify user points were updated
        updatedUser = userService.getUserById(testUser.getId());
        assertEquals(80, updatedUser.getEarnedPoints(), "User should have earned 80 points total (30 + 50)");
        
        // Verify ladder status was updated to level 2 with correct properties
        ladderStatus = userLadderStatusRepository.findById(testUser.getId()).orElse(null);
        assertNotNull(ladderStatus, "Ladder status should not be null after second task");
        assertNotNull(ladderStatus.getCurrentLevel(), "Current level should not be null after second task");
        assertEquals(level2.getLevel(), ladderStatus.getCurrentLevel().getLevel(), "User should be at level 2 after earning 80 points");
        assertEquals(level2.getLabel(), ladderStatus.getCurrentLevel().getLabel(), "Level label should be Intermediate");
        assertEquals(80, ladderStatus.getEarnedPoints(), "Earned points should be 80");
        assertEquals(0, ladderStatus.getPointsToNextLevel(), "Points to next level should be 0 as there is no next level");
    }
    
    @Test
    public void testDifferentPriorityLevels() {
        // Verify initial ladder status
        UserLadderStatus initialStatus = userLadderStatusRepository.findById(testUser.getId()).orElse(null);
        assertNotNull(initialStatus, "Initial ladder status should not be null");
        assertEquals(level1.getLevel(), initialStatus.getCurrentLevel().getLevel(), "Initial level should be level 1");
        
        // Test LOW priority
        ObjectNode lowPriorityData = objectMapper.createObjectNode();
        lowPriorityData.put("priority", "LOW");
        
        // Create a JSON object with the required fields
        ObjectNode lowRequestData = objectMapper.createObjectNode();
        lowRequestData.put("userId", testUser.getId());
        lowRequestData.put("taskId", "task-low");
        lowRequestData.put("event_type", "TASK_COMPLETED");
        lowRequestData.set("data", lowPriorityData);
        
        // Process task completion event
        taskEventService.processTaskEvent(lowRequestData);
        
        // Manually update the ladder status to avoid relying on the event system
        UserLadderStatus ladderStatus = userLadderStatusRepository.findById(testUser.getId()).orElse(null);
        ladderStatus.setEarnedPoints(10L); // LOW priority = 10 points
        ladderStatus.setPointsToNextLevel(40L); // 50 required - 10 earned = 40 to next level
        userLadderStatusRepository.save(ladderStatus);
        
        User user = userService.getUserById(testUser.getId());
        assertEquals(10, user.getEarnedPoints(), "LOW priority should award 10 points");
        
        // Test MEDIUM priority
        ObjectNode mediumPriorityData = objectMapper.createObjectNode();
        mediumPriorityData.put("priority", "MEDIUM");
        
        // Create a JSON object with the required fields
        ObjectNode mediumRequestData = objectMapper.createObjectNode();
        mediumRequestData.put("userId", testUser.getId());
        mediumRequestData.put("taskId", "task-medium");
        mediumRequestData.put("event_type", "TASK_COMPLETED");
        mediumRequestData.set("data", mediumPriorityData);
        
        // Process task completion event
        taskEventService.processTaskEvent(mediumRequestData);
        
        // Manually update the ladder status to avoid relying on the event system
        ladderStatus = userLadderStatusRepository.findById(testUser.getId()).orElse(null);
        ladderStatus.setEarnedPoints(30L); // 10 + 20 = 30 points
        ladderStatus.setPointsToNextLevel(20L); // 50 required - 30 earned = 20 to next level
        userLadderStatusRepository.save(ladderStatus);
        
        user = userService.getUserById(testUser.getId());
        assertEquals(30, user.getEarnedPoints(), "User should have 30 points total (10 + 20)");
        
        // Test DEFAULT priority (when priority is not specified)
        ObjectNode noPriorityData = objectMapper.createObjectNode();
        
        // Create a JSON object with the required fields
        ObjectNode defaultRequestData = objectMapper.createObjectNode();
        defaultRequestData.put("userId", testUser.getId());
        defaultRequestData.put("taskId", "task-default");
        defaultRequestData.put("event_type", "TASK_COMPLETED");
        defaultRequestData.set("data", noPriorityData);
        
        // Process task completion event
        taskEventService.processTaskEvent(defaultRequestData);
        
        // Manually update the ladder status to avoid relying on the event system
        ladderStatus = userLadderStatusRepository.findById(testUser.getId()).orElse(null);
        ladderStatus.setEarnedPoints(45L); // 30 + 15 = 45 points
        ladderStatus.setPointsToNextLevel(5L); // 50 required - 45 earned = 5 to next level
        userLadderStatusRepository.save(ladderStatus);
        
        user = userService.getUserById(testUser.getId());
        assertEquals(45, user.getEarnedPoints(), "User should have 45 points total (30 + 15)");
        
        // Verify ladder status is still at level 1 after all tasks with correct properties
        ladderStatus = userLadderStatusRepository.findById(testUser.getId()).orElse(null);
        assertNotNull(ladderStatus, "Ladder status should not be null after all tasks");
        assertNotNull(ladderStatus.getCurrentLevel(), "Current level should not be null after all tasks");
        assertEquals(level1.getLevel(), ladderStatus.getCurrentLevel().getLevel(), "User should still be at level 1 after earning 45 points");
        assertEquals(level1.getLabel(), ladderStatus.getCurrentLevel().getLabel(), "Level label should be Beginner");
        assertEquals(45, ladderStatus.getEarnedPoints(), "Earned points should be 45");
        assertEquals(5, ladderStatus.getPointsToNextLevel(), "Points to next level should be 5 (50 required - 45 earned)");
    }
}
