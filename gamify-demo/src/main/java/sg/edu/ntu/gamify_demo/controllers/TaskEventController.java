package sg.edu.ntu.gamify_demo.controllers;

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

import sg.edu.ntu.gamify_demo.Services.TaskEventService;
import sg.edu.ntu.gamify_demo.dtos.TaskEventDTO;
import sg.edu.ntu.gamify_demo.mappers.TaskEventMapper;
import sg.edu.ntu.gamify_demo.models.TaskEvent;

/**
 * REST controller for task event-related endpoints.
 * Uses TaskEventDTO to limit response fields and avoid nested issues.
 */
@RestController
@RequestMapping("/tasks")
public class TaskEventController {

    private final TaskEventService taskEventService;
    private final TaskEventMapper taskEventMapper;
    private final ObjectMapper objectMapper;

    /**
     * Constructor for dependency injection.
     */
    public TaskEventController(TaskEventService taskEventService, TaskEventMapper taskEventMapper, ObjectMapper objectMapper) {
        this.taskEventService = taskEventService;
        this.taskEventMapper = taskEventMapper;
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
            // Validate required fields
            if (!eventData.has("userId") || !eventData.has("taskId") || !eventData.has("event_type")) {
                ObjectNode errorResponse = objectMapper.createObjectNode();
                errorResponse.put("error", "Bad Request");
                errorResponse.put("message", "Missing required fields: userId, taskId, and event_type");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            ObjectNode response = taskEventService.processTaskEvent(eventData);
            
            // Add priority to response if available
            if (eventData.has("data") && eventData.get("data").has("priority")) {
                response.put("priority", eventData.get("data").get("priority").asText());
            }
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("error", "Bad Request");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", "An unexpected error occurred");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get a task event by its ID.
     * 
     * @param eventId The ID of the event.
     * @return The task event DTO.
     */
    @GetMapping("/events/{eventId}")
    public ResponseEntity<TaskEventDTO> getTaskEvent(@PathVariable String eventId) {
        TaskEvent taskEvent = taskEventService.getTaskEventById(eventId);
        if (taskEvent == null) {
            return ResponseEntity.notFound().build();
        }
        TaskEventDTO taskEventDTO = taskEventMapper.toDTO(taskEvent);
        return ResponseEntity.ok(taskEventDTO);
    }
}
