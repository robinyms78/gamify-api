package sg.edu.ntu.gamify_demo.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.swagger.v3.oas.annotations.Operation;
import sg.edu.ntu.gamify_demo.dtos.UserAchievementDTO;
import sg.edu.ntu.gamify_demo.exceptions.AchievementNotFoundException;
import sg.edu.ntu.gamify_demo.exceptions.UserNotFoundException;
import sg.edu.ntu.gamify_demo.facades.GamificationFacade;
import sg.edu.ntu.gamify_demo.interfaces.AchievementService;
import sg.edu.ntu.gamify_demo.interfaces.UserAchievementService;
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.models.Achievement;
import sg.edu.ntu.gamify_demo.models.User;

/**
 * REST controller for achievement-related endpoints.
 */
@RestController
@RequestMapping("/api/achievements")
public class AchievementController {
    
    private final AchievementService achievementService;
    private final UserService userService;
    private final UserAchievementService userAchievementService;
    private final GamificationFacade gamificationFacade;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor for dependency injection.
     */
    @Autowired
    public AchievementController(
            AchievementService achievementService,
            UserService userService,
            UserAchievementService userAchievementService,
            GamificationFacade gamificationFacade,
            ObjectMapper objectMapper) {
        this.achievementService = achievementService;
        this.userService = userService;
        this.userAchievementService = userAchievementService;
        this.gamificationFacade = gamificationFacade;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Get all achievements.
     * 
     * @return A list of all achievements.
     */
    @GetMapping
    @Operation(summary = "Get all achievements", description = "Retrieves a list of all available achievements")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved achievements list",
               content = @Content(schema = @Schema(implementation = List.class)))
    public ResponseEntity<List<Achievement>> getAllAchievements() {
        List<Achievement> achievements = gamificationFacade.getAllAchievements();
        return ResponseEntity.ok(achievements);
    }
    
    /**
     * Get an achievement by its ID.
     * 
     * @param achievementId The ID of the achievement.
     * @return The achievement if found.
     */
    @GetMapping("/{achievementId}")
    @Operation(summary = "Get achievement details", description = "Retrieves details of a specific achievement")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Achievement found",
                    content = @Content(schema = @Schema(implementation = Achievement.class))),
        @ApiResponse(responseCode = "404", description = "Achievement not found")
    })
    public ResponseEntity<Achievement> getAchievementById(
        @Parameter(description = "ID of the achievement to retrieve", example = "achieve-123")
        @PathVariable String achievementId) {
        Achievement achievement = achievementService.getAchievementById(achievementId);
        return ResponseEntity.ok(achievement);
    }
    
    /**
     * Create a new achievement.
     * 
     * @param achievementData JSON data containing name, description, and criteria.
     * @return The created achievement.
     */
    @PostMapping
    @Operation(summary = "Create achievement", description = "Creates a new achievement in the system")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Achievement created successfully",
                    content = @Content(schema = @Schema(implementation = Achievement.class))),
        @ApiResponse(responseCode = "400", description = "Invalid achievement data")
    })
    public ResponseEntity<Achievement> createAchievement(
        @Parameter(description = "Achievement data in JSON format", required = true,
                  content = @Content(schema = @Schema(example = """
                      {
                          "name": "Master Explorer", 
                          "description": "Complete 50 tasks",
                          "criteria": {"tasksCompleted": 50}
                      }""")))
        @RequestBody JsonNode achievementData) {
        String name = achievementData.get("name").asText();
        String description = achievementData.get("description").asText();
        JsonNode criteria = achievementData.get("criteria");
        
        Achievement achievement = achievementService.createAchievement(name, description, criteria);
        return new ResponseEntity<>(achievement, HttpStatus.CREATED);
    }
    
    /**
     * Update an existing achievement.
     * 
     * @param achievementId The ID of the achievement to update.
     * @param achievementData JSON data containing name, description, and criteria.
     * @return The updated achievement.
     */
    @PutMapping("/{achievementId}")
    @Operation(summary = "Update achievement", description = "Updates an existing achievement")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Achievement updated successfully",
                    content = @Content(schema = @Schema(implementation = Achievement.class))),
        @ApiResponse(responseCode = "404", description = "Achievement not found"),
        @ApiResponse(responseCode = "400", description = "Invalid update data")
    })
    public ResponseEntity<Achievement> updateAchievement(
        @Parameter(description = "ID of the achievement to update", example = "achieve-456")
        @PathVariable String achievementId,
        @RequestBody JsonNode achievementData) {
        
        String name = achievementData.get("name").asText();
        String description = achievementData.get("description").asText();
        JsonNode criteria = achievementData.get("criteria");
        
        Achievement achievement = achievementService.updateAchievement(achievementId, name, description, criteria);
        return ResponseEntity.ok(achievement);
    }
    
    /**
     * Delete an achievement.
     * 
     * @param achievementId The ID of the achievement to delete.
     * @return No content response.
     */
    @DeleteMapping("/{achievementId}")
    @Operation(summary = "Delete achievement", description = "Removes an achievement from the system")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Achievement deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Achievement not found")
    })
    public ResponseEntity<Void> deleteAchievement(
        @Parameter(description = "ID of the achievement to delete", example = "achieve-789")
        @PathVariable String achievementId) {
        achievementService.deleteAchievement(achievementId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get a user's achievements.
     * 
     * @param userId The ID of the user.
     * @return The user's achievements.
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user achievements", description = "Retrieves all achievements earned by a user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved user achievements",
                    content = @Content(schema = @Schema(implementation = UserAchievementDTO.class))),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserAchievementDTO> getUserAchievements(
        @Parameter(description = "ID of the user", example = "user-123")
        @PathVariable String userId) {
        UserAchievementDTO userAchievements = gamificationFacade.getUserAchievements(userId);
        return ResponseEntity.ok(userAchievements);
    }
    
    /**
     * Check if a user has a specific achievement.
     * 
     * @param achievementId The ID of the achievement.
     * @param userId The ID of the user.
     * @return True if the user has the achievement, false otherwise.
     */
    @GetMapping("/{achievementId}/check/{userId}")
    @Operation(summary = "Check achievement progress", 
              description = "Checks if a user has earned a specific achievement")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check completed",
                    content = @Content(schema = @Schema(implementation = ObjectNode.class))),
        @ApiResponse(responseCode = "404", description = "User or achievement not found")
    })
    public ResponseEntity<ObjectNode> checkUserAchievement(
        @Parameter(description = "ID of the achievement to check", example = "achieve-123")
        @PathVariable String achievementId,
        @Parameter(description = "ID of the user to check", example = "user-456")
        @PathVariable String userId) {
        
        Achievement achievement = achievementService.getAchievementById(achievementId);
        User user = userService.getUserById(userId);
        
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        
        boolean hasAchievement = userAchievementService.hasAchievement(user, achievement);
        
        ObjectNode result = objectMapper.createObjectNode();
        result.put("hasAchievement", hasAchievement);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Process an event for a user.
     * 
     * @param userId The ID of the user.
     * @param eventData JSON data containing eventType and additional event data.
     * @return Success response.
     */
    @PostMapping("/process/{userId}")
    @Operation(summary = "Process achievement event", 
              description = "Handles events that might trigger achievement unlocks")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Event processed successfully",
                    content = @Content(schema = @Schema(implementation = ObjectNode.class))),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "400", description = "Invalid event data")
    })
    public ResponseEntity<ObjectNode> processEvent(
        @Parameter(description = "ID of the user", example = "user-789")
        @PathVariable String userId,
        @Parameter(description = "Event data in JSON format", required = true,
                  content = @Content(schema = @Schema(example = """
                      {
                          "eventType": "TASK_COMPLETED", 
                          "eventDetails": {"taskType": "BUG_FIX"}
                      }""")))
        @RequestBody JsonNode eventData) {
        
        String eventType = eventData.get("eventType").asText();
        JsonNode eventDetails = eventData.get("eventDetails");
        
        gamificationFacade.processEvent(eventType, userId, eventDetails);
        
        ObjectNode result = objectMapper.createObjectNode();
        result.put("success", true);
        result.put("message", "Event processed successfully");
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Exception handler for AchievementNotFoundException.
     * 
     * @param ex The exception.
     * @return Error response.
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(AchievementNotFoundException.class)
    public ResponseEntity<ObjectNode> handleAchievementNotFoundException(AchievementNotFoundException ex) {
        ObjectNode errorJson = objectMapper.createObjectNode();
        errorJson.put("error", "Achievement not found");
        errorJson.put("message", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorJson);
    }
    
    /**
     * Exception handler for UserNotFoundException.
     * 
     * @param ex The exception.
     * @return Error response.
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ObjectNode> handleUserNotFoundException(UserNotFoundException ex) {
        ObjectNode errorJson = objectMapper.createObjectNode();
        errorJson.put("error", "User not found");
        errorJson.put("message", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorJson);
    }
}
