package sg.edu.ntu.gamify_demo.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import sg.edu.ntu.gamify_demo.services.GamificationService;
import sg.edu.ntu.gamify_demo.services.LadderService;
import sg.edu.ntu.gamify_demo.services.TaskEventService;
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.mappers.TaskEventMapper;
import sg.edu.ntu.gamify_demo.models.TaskEvent;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.enums.TaskStatus;


@WebMvcTest(TaskEventController.class)
public class TaskEventControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private GamificationService gamificationService;

    @MockBean
    private TaskEventService taskEventService;
    
    @MockBean
    private UserService userService;
    
    @MockBean
    private LadderService ladderService;
    
    @MockBean
    private TaskEventMapper taskEventMapper;
    
    
    private User testUser;
    private TaskEvent testTaskEvent;
    
    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
            
        reset(taskEventService); // Clear previous mock states
        
        // Create a test user
        testUser = new User();
        testUser.setId("user123");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setEarnedPoints(100L);
        testUser.setAvailablePoints(100L);
        
        // Create a test task event
        testTaskEvent = new TaskEvent();
        testTaskEvent.setEventId(UUID.randomUUID().toString());
        testTaskEvent.setUser(testUser);
        testTaskEvent.setTaskId("task123");
        testTaskEvent.setEventType("TASK_COMPLETED");
        testTaskEvent.setStatus(TaskStatus.COMPLETED);
        testTaskEvent.setCompletionTime(ZonedDateTime.now());
        
        // Mock service responses
        when(userService.getUserById(anyString())).thenReturn(testUser);
        
        // Create a mock response for processTaskEvent
        ObjectNode mockResponse = objectMapper.createObjectNode();
        mockResponse.put("success", true);
        mockResponse.put("eventId", testTaskEvent.getEventId());
        mockResponse.put("userId", "user123");
        mockResponse.put("taskId", "task123");
        mockResponse.put("eventType", "TASK_COMPLETED");
        mockResponse.put("status", "COMPLETED");
        mockResponse.put("pointsAwarded", 30);
        
        when(taskEventService.processTaskEvent(any(JsonNode.class)))
            .thenReturn(mockResponse);
        when(taskEventService.getTaskEventById(anyString()))
            .thenReturn(testTaskEvent);
        ObjectNode taskData = objectMapper.createObjectNode();
        taskData.put("priority", "HIGH");
        when(taskEventService.calculatePointsForTask(anyString(), any(JsonNode.class)))
            .thenReturn(30); // High priority task
    }
    
    @Test
    public void testProcessTaskCompletionEvent() throws Exception {
        // Create request body
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("userId", "user123");
        requestBody.put("taskId", "task123");
        requestBody.put("event_type", "TASK_COMPLETED");
        
        ObjectNode data = objectMapper.createObjectNode();
        data.put("priority", "HIGH");
        requestBody.set("data", data);
        
        // Mock the response to include priority
        ObjectNode mockResponse = objectMapper.createObjectNode();
        mockResponse.put("success", true);
        mockResponse.put("eventId", testTaskEvent.getEventId());
        mockResponse.put("userId", "user123");
        mockResponse.put("taskId", "task123");
        mockResponse.put("eventType", "TASK_COMPLETED");
        mockResponse.put("status", "COMPLETED");
        mockResponse.put("pointsAwarded", 30);
        mockResponse.put("priority", "HIGH");
        
        when(taskEventService.processTaskEvent(any(JsonNode.class)))
            .thenReturn(mockResponse);
        
        // Perform request and verify response
        mockMvc.perform(post("/tasks/events")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.eventId").exists())
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.taskId").value("task123"))
                .andExpect(jsonPath("$.eventType").value("TASK_COMPLETED"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.pointsAwarded").value(30))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                ;
        
        // Verify service method calls - only verify processTaskEvent
        verify(taskEventService, times(1)).processTaskEvent(any(JsonNode.class));
    }
    
    @Test
    public void testMissingRequiredFields() throws Exception {
        // Create request body with missing fields
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("userId", "user123");
        // Missing taskId and event_type
        
        // Perform request and verify response
        mockMvc.perform(post("/tasks/events")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Missing required fields: userId, taskId, and event_type"));
    }
    
    @Test
    @DisplayName("Test invalid event type")
    public void testInvalidEventType() throws Exception {
        // Create request body with invalid event type
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("userId", "user123");
        requestBody.put("taskId", "task123");
        requestBody.put("event_type", "INVALID_EVENT");
        
        // Mock service to throw exception for invalid event type
        when(taskEventService.processTaskEvent(any(JsonNode.class)))
            .thenThrow(new IllegalArgumentException("Invalid event type: INVALID_EVENT"));
        
        // Perform request and verify response
        mockMvc.perform(post("/tasks/events")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid event type: INVALID_EVENT"));
    }
    @Test
    @DisplayName("Test task assignment event")
    void testTaskAssignmentEvent() throws Exception {
        // Mock specific assignment response
        ObjectNode assignmentResponse = objectMapper.createObjectNode();
        assignmentResponse.put("success", true);
        assignmentResponse.put("eventType", "TASK_ASSIGNED");
        assignmentResponse.put("status", "ASSIGNED");
        
        when(taskEventService.processTaskEvent(any(JsonNode.class)))
            .thenReturn(assignmentResponse);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("userId", "user123");
        requestBody.put("taskId", "task456");
        requestBody.put("event_type", "TASK_ASSIGNED");
        
        ObjectNode data = objectMapper.createObjectNode();
        data.put("dueDate", "2024-12-31T23:59:59Z");
        requestBody.set("data", data);

        mockMvc.perform(post("/tasks/events")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventType").value("TASK_ASSIGNED"))
                .andExpect(jsonPath("$.status").value("ASSIGNED"));
    }

    @Test
    @DisplayName("Test invalid JSON payload")
    void testInvalidJsonPayload() throws Exception {
        mockMvc.perform(post("/tasks/events")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid-json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Test unauthorized access")
    void testUnauthorizedAccess() throws Exception {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("userId", "user123");
        requestBody.put("taskId", "task123");
        requestBody.put("event_type", "TASK_COMPLETED");

        mockMvc.perform(post("/tasks/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isUnauthorized());
    }
}
