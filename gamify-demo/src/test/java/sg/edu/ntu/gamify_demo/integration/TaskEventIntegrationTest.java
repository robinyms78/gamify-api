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

import sg.edu.ntu.gamify_demo.Services.LadderService;
import sg.edu.ntu.gamify_demo.Services.TaskEventService;
import sg.edu.ntu.gamify_demo.config.TestIntegrationConfig;
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
@ActiveProfiles("test")
@Transactional
@Import(TestIntegrationConfig.class)
public class TaskEventIntegrationTest {

    @Autowired
    private TaskEventService taskEventService;
    
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
        // Create test user
        testUser = new User();
        testUser.setId(java.util.UUID.randomUUID().toString());
        testUser.setUsername("taskuser");
        testUser.setEmail("taskuser@example.com");
        testUser.setPasswordHash("password");
        testUser.setRole(sg.edu.ntu.gamify_demo.models.enums.UserRole.EMPLOYEE);
        testUser.setEarnedPoints(0);
        testUser.setAvailablePoints(0);
        testUser = userRepository.save(testUser);
        
        // Create ladder levels
        level1 = new LadderLevel();
        level1.setLevel(1);
        level1.setLabel("Beginner");
        level1.setPointsRequired(0);
        level1 = ladderLevelRepository.save(level1);
        
        level2 = new LadderLevel();
        level2.setLevel(2);
        level2.setLabel("Intermediate");
        level2.setPointsRequired(50);
        level2 = ladderLevelRepository.save(level2);
        
        // Initialize user ladder status
        ladderService.initializeUserLadderStatus(testUser);
    }
    
    @Test
    public void testTaskCompletionAndPointsAwarding() {
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
        
        // Verify task event was created
        assertNotNull(taskEvent);
        assertEquals(testUser, taskEvent.getUser());
        assertEquals(taskId, taskEvent.getTaskId());
        assertEquals(eventType, taskEvent.getEventType());
        assertEquals(TaskStatus.COMPLETED, taskEvent.getStatus());
        assertNotNull(taskEvent.getCompletionTime());
        
        // Verify points transaction was created
        List<PointsTransaction> transactions = pointsTransactionRepository.findByUserAndEventType(testUser, "TASK_COMPLETED");
        assertTrue(!transactions.isEmpty());
        
        PointsTransaction transaction = transactions.get(0);
        assertEquals(testUser, transaction.getUser());
        assertEquals("TASK_COMPLETED", transaction.getEventType());
        assertEquals(30, transaction.getPoints()); // HIGH priority = 30 points
        
        // Verify user points were updated
        User updatedUser = userService.getUserById(testUser.getId());
        assertEquals(30, updatedUser.getEarnedPoints());
        assertEquals(30, updatedUser.getAvailablePoints());
        
        // Verify ladder status is still at level 1
        UserLadderStatus ladderStatus = userLadderStatusRepository.findById(testUser.getId()).orElse(null);
        assertNotNull(ladderStatus);
        assertEquals(level1.getLevel(), ladderStatus.getCurrentLevel().getLevel());
        assertEquals(20, ladderStatus.getPointsToNextLevel()); // 50 required - 30 earned = 20 to next level
        
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
        
        // Verify user points were updated
        updatedUser = userService.getUserById(testUser.getId());
        assertEquals(80, updatedUser.getEarnedPoints()); // 30 + 50 = 80
        
        // Verify ladder status was updated to level 2
        ladderStatus = userLadderStatusRepository.findById(testUser.getId()).orElse(null);
        assertNotNull(ladderStatus);
        assertEquals(level2.getLevel(), ladderStatus.getCurrentLevel().getLevel());
    }
    
    @Test
    public void testDifferentPriorityLevels() {
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
        
        User user = userService.getUserById(testUser.getId());
        assertEquals(10, user.getEarnedPoints()); // LOW = 10 points
        
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
        
        user = userService.getUserById(testUser.getId());
        assertEquals(30, user.getEarnedPoints()); // 10 + 20 = 30
        
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
        
        user = userService.getUserById(testUser.getId());
        assertEquals(45, user.getEarnedPoints()); // 30 + 15 = 45
    }
}
