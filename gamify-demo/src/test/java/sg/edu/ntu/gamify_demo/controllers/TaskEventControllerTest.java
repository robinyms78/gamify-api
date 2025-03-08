package sg.edu.ntu.gamify_demo.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.models.TaskEvent;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.enums.TaskStatus;
import sg.edu.ntu.gamify_demo.services.GamificationService;
import sg.edu.ntu.gamify_demo.services.LadderService;
import sg.edu.ntu.gamify_demo.services.TaskEventService;

@WebMvcTest(TaskEventController.class)
public class TaskEventControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private TaskEventService taskEventService;
    
    @MockBean
    private UserService userService;
    
    @MockBean
    private GamificationService gamificationService;
    
    @MockBean
    private LadderService ladderService;
    
    private User testUser;
    private TaskEvent testTaskEvent;
    
    @BeforeEach
    public void setup() {
        // Create a test user
        testUser = new User();
        testUser.setId("user123");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setEarnedPoints(100);
        testUser.setAvailablePoints(100);
        
        // Create a test task event
        testTaskEvent = new TaskEvent();
        testTaskEvent.setEventId(UUID.randomUUID().toString());
        testTaskEvent.setUser(testUser);
        testTaskEvent.setTaskId("task123");
        testTaskEvent.setEventType("TASK_COMPLETED");
        testTaskEvent.setStatus(TaskStatus.COMPLETED);
        testTaskEvent.setCompletionTime(LocalDateTime.now());
        
        // Mock service responses
        when(userService.getUserById(anyString())).thenReturn(testUser);
        when(taskEventService.processTaskEvent(anyString(), anyString(), anyString(), any(JsonNode.class)))
            .thenReturn(testTaskEvent);
        when(taskEventService.calculatePointsForTask(anyString(), any(JsonNode.class)))
            .thenReturn(30); // High priority task
        when(gamificationService.awardPoints(anyString(), anyInt(), anyString(), any(JsonNode.class)))
            .thenReturn(testUser.getEarnedPoints() + 30);
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
        
        // Perform request and verify response
        mockMvc.perform(post("/tasks/events")
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
                .andExpect(jsonPath("$.priority").value("HIGH"));
        
        // Verify service method calls
        verify(taskEventService, times(1)).processTaskEvent(
                eq("user123"), 
                eq("task123"), 
                eq("TASK_COMPLETED"), 
                any(JsonNode.class));
        
        verify(taskEventService, times(1)).calculatePointsForTask(
                eq("task123"), 
                any(JsonNode.class));
        
        verify(ladderService, times(1)).updateUserLadderStatus(eq("user123"));
    }
    
    @Test
    public void testMissingRequiredFields() throws Exception {
        // Create request body with missing fields
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("userId", "user123");
        // Missing taskId and event_type
        
        // Perform request and verify response
        mockMvc.perform(post("/tasks/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists());
    }
}
