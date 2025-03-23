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
import sg.edu.ntu.gamify_demo.dtos.TaskEventRequestDTO;
import sg.edu.ntu.gamify_demo.dtos.ErrorResponseDTO;
import sg.edu.ntu.gamify_demo.models.enums.TaskStatus;

import jakarta.validation.Valid;
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
        @ApiResponse(responseCode = "400", description = "Invalid request format/missing fields",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<Object> processTaskEvent(
        @Parameter(description = "Event data in JSON format", required = true,
                  content = @Content(schema = @Schema(implementation = TaskEventRequestDTO.class)))
        @RequestBody @Valid TaskEventRequestDTO eventRequest) {
        try {
            // Convert DTO to JsonNode for service processing
            ObjectNode eventNode = objectMapper.createObjectNode();
            eventNode.put("userId", eventRequest.getUserId());
            eventNode.put("taskId", eventRequest.getTaskId());
            eventNode.put("event_type", eventRequest.getEventType());
            
            if (eventRequest.getData() != null) {
                eventNode.set("data", eventRequest.getData());
            }
            
            // Process the event
            ObjectNode serviceResponse = taskEventService.processTaskEvent(eventNode);
            
            // Map the service response to DTO
            TaskEventResponseDTO response = new TaskEventResponseDTO();
            response.setSuccess(serviceResponse.get("success").asBoolean());
            response.setEventId(serviceResponse.get("eventId").asText());
            response.setUserId(serviceResponse.get("userId").asText());
            response.setTaskId(serviceResponse.get("taskId").asText());
            response.setEventType(serviceResponse.get("eventType").asText());
            response.setStatus(TaskStatus.fromValue(serviceResponse.get("status").asText()));
            
            // Add priority to response if available
            JsonNode data = eventRequest.getData();
            if (data != null && data.has("priority")) {
                response.setPriority(data.get("priority").asText());
            }
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ErrorResponseDTO errorResponse = new ErrorResponseDTO();
            errorResponse.setError("Bad Request");
            errorResponse.setMessage(e.getMessage());
            // timestamp is automatically set by the DTO
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            ErrorResponseDTO errorResponse = new ErrorResponseDTO();
            errorResponse.setError("Internal Server Error");
            errorResponse.setMessage("An unexpected error occurred: " + e.getMessage());
            // timestamp is automatically set by the DTO
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
        @ApiResponse(responseCode = "404", description = "Task event not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<?> getTaskEvent(
        @Parameter(description = "ID of the event to retrieve", example = "event-12345")
        @PathVariable String eventId) {
        TaskEvent taskEvent = taskEventService.getTaskEventById(eventId);
        if (taskEvent == null) {
            ErrorResponseDTO errorResponse = new ErrorResponseDTO();
            errorResponse.setError("Not Found");
            errorResponse.setMessage("Task event with ID " + eventId + " not found");
            // timestamp is automatically set by the DTO
            return ResponseEntity.status(404).body(errorResponse);
        }
        TaskEventDTO taskEventDTO = taskEventMapper.toDTO(taskEvent);
        return ResponseEntity.ok(taskEventDTO);
    }
}
