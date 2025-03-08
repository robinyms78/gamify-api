package sg.edu.ntu.gamify_demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sg.edu.ntu.gamify_demo.models.TaskEvent;
import sg.edu.ntu.gamify_demo.services.TaskEventService;

/**
 * REST controller for task event-related endpoints.
 * This controller follows the Facade pattern to provide a simple interface for task event operations.
 */
@RestController
@RequestMapping("/tasks")
public class TaskEventController {
    
    private final TaskEventService taskEventService;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor for dependency injection.
     */
    @Autowired
    public TaskEventController(TaskEventService taskEventService, ObjectMapper objectMapper) {
        this.taskEventService = taskEventService;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Process a task event.
     * 
     * @param eventData JSON data containing userId, taskId, eventType, and additional event data.
     * @return The processed task event and any additional information.
     */
    @PostMapping("/events")
    public ResponseEntity<ObjectNode> processTaskEvent(@RequestBody JsonNode eventData) {
        try {
            // Extract required fields from the event data
            String userId = eventData.has("userId") ? eventData.get("userId").asText() : null;
            String taskId = eventData.has("taskId") ? eventData.get("taskId").asText() : null;
            String eventType = eventData.has("event_type") ? eventData.get("event_type").asText() : null;
            JsonNode additionalData = eventData.has("data") ? eventData.get("data") : objectMapper.createObjectNode();
            
            // Validate required fields
            if (userId == null || taskId == null || eventType == null) {
                ObjectNode errorResponse = objectMapper.createObjectNode();
                errorResponse.put("error", "Missing required fields");
                errorResponse.put("message", "userId, taskId, and event_type are required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Process the task event
            TaskEvent taskEvent = taskEventService.processTaskEvent(userId, taskId, eventType, additionalData);
            
            // Create response
            ObjectNode response = objectMapper.createObjectNode();
            response.put("success", true);
            response.put("eventId", taskEvent.getEventId());
            response.put("userId", userId);
            response.put("taskId", taskId);
            response.put("eventType", eventType);
            response.put("status", taskEvent.getStatus().toString());
            
            // Add points information if this was a task completion event
            if ("TASK_COMPLETED".equals(eventType)) {
                String priority = additionalData.has("priority") ? 
                        additionalData.get("priority").asText() : "DEFAULT";
                
                int pointsAwarded = taskEventService.calculatePointsForTask(taskId, additionalData);
                response.put("pointsAwarded", pointsAwarded);
                response.put("priority", priority);
            }
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("error", "Invalid request");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("error", "Server error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get a task event by its ID.
     * 
     * @param eventId The ID of the event.
     * @return The task event.
     */
    @GetMapping("/events/{eventId}")
    public ResponseEntity<Object> getTaskEvent(@PathVariable String eventId) {
        TaskEvent taskEvent = taskEventService.getTaskEventById(eventId);
        
        if (taskEvent == null) {
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("error", "Not found");
            errorResponse.put("message", "Task event not found: " + eventId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        return ResponseEntity.ok(taskEvent);
    }
}
