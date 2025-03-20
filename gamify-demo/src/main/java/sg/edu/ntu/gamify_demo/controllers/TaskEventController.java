package sg.edu.ntu.gamify_demo.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sg.edu.ntu.gamify_demo.services.TaskEventService;
import sg.edu.ntu.gamify_demo.dtos.TaskEventDTO;
import sg.edu.ntu.gamify_demo.dtos.TaskEventResponseDTO;
import sg.edu.ntu.gamify_demo.mappers.TaskEventMapper;
import sg.edu.ntu.gamify_demo.models.TaskEvent;

/**
 * REST controller for task event-related endpoints.
 * Uses TaskEventDTO to limit response fields and avoid nested issues.
 */
@RestController
@RequestMapping("/tasks")
@Tag(name = "Task Events", description = "Operations related to task events processing")
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
    @Operation(summary = "Process task event", 
               description = "Processes a task-related event and updates user progress")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Task event processed successfully",
                    content = @Content(schema = @Schema(implementation = TaskEventResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request format/missing fields"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ObjectNode> processTaskEvent(
        @Parameter(description = "Event data in JSON format", required = true,
                  content = @Content(schema = @Schema(example = """
                      {
                          "userId": "123e4567-e89b-12d3-a456-426614174000",
                          "taskId": "task-789",
                          "event_type": "COMPLETION",
                          "data": {
                              "priority": "HIGH",
                              "details": "Additional event information"
                          }
                      }""")))
        @RequestBody JsonNode eventData) {
        try {
            // Validate required fields
            if (!eventData.has("userId") || !eventData.has("taskId") || !eventData.has("event_type")) {
                ObjectNode errorResponse = objectMapper.createObjectNode();
                errorResponse.put("error", "Bad Request");
                errorResponse.put("message", "Missing required fields: userId, taskId, and event_type");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Sanitize the event data to remove any control characters
            String sanitizedJson = eventData.toString().replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
            JsonNode sanitizedEventData = objectMapper.readTree(sanitizedJson);
            
            ObjectNode response = taskEventService.processTaskEvent(sanitizedEventData);
            
            // Add priority to response if available
            if (sanitizedEventData.has("data") && sanitizedEventData.get("data").has("priority")) {
                response.put("priority", sanitizedEventData.get("data").get("priority").asText());
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
            errorResponse.put("message", "An unexpected error occurred: " + e.getMessage());
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
    @Operation(summary = "Get task event by ID", 
              description = "Retrieves detailed information about a specific task event")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved task event",
                    content = @Content(schema = @Schema(implementation = TaskEventDTO.class))),
        @ApiResponse(responseCode = "404", description = "Task event not found")
    })
    public ResponseEntity<TaskEventDTO> getTaskEvent(
        @Parameter(description = "ID of the event to retrieve", example = "event-12345")
        @PathVariable String eventId) {
        TaskEvent taskEvent = taskEventService.getTaskEventById(eventId);
        if (taskEvent == null) {
            return ResponseEntity.notFound().build();
        }
        TaskEventDTO taskEventDTO = taskEventMapper.toDTO(taskEvent);
        return ResponseEntity.ok(taskEventDTO);
    }
}
