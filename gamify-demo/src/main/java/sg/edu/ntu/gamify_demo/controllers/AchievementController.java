package sg.edu.ntu.gamify_demo.controllers;

import java.util.List;

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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import sg.edu.ntu.gamify_demo.dtos.UserAchievementDTO;
import sg.edu.ntu.gamify_demo.exceptions.AchievementNotFoundException;
import sg.edu.ntu.gamify_demo.exceptions.UserNotFoundException;
import sg.edu.ntu.gamify_demo.facades.GamificationFacade;
import sg.edu.ntu.gamify_demo.interfaces.AchievementService;
import sg.edu.ntu.gamify_demo.interfaces.UserAchievementService;
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.models.Achievement;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.UserAchievement;

/**
 * REST controller for achievement-related endpoints.
 */
@RestController
@RequestMapping("/api/achievements")
@Tag(name = "Achievements", description = "Endpoints for managing achievements and user achievement tracking")
@SecurityRequirement(name = "bearerAuth")
public class AchievementController {
    
    private final AchievementService achievementService;
    private final UserService userService;
    private final UserAchievementService userAchievementService;
    private final GamificationFacade gamificationFacade;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor for dependency injection.
     */
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
    @ApiResponse(responseCode = "200", description = "Success",
               content = @Content(array = @ArraySchema(
                   schema = @Schema(implementation = AchievementDTO.class))),
               examples = @ExampleObject("""
                   [
                     {
                       "id": "achieve-123",
                       "name": "Marathon Runner",
                       "description": "Complete 100 tasks",
                       "criteria": {"tasksCompleted": 100}
                     },
                     {
                       "id": "achieve-456", 
                       "name": "Speed Demon",
                       "description": "Complete 5 tasks in 1 hour",
                       "criteria": {"tasksInHour": 5}
                     }
                   ]""")))
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
        @ApiResponse(responseCode = "400", description = "Invalid achievement data",
                    content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(hidden = true)))
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
    @Parameter(name = "achievementId", example = "achieve-1122", 
              schema = @Schema(pattern = "^achieve-[a-zA-Z0-9]{8}$"))
    @Parameter(name = "userId", example = "user-3344",
              schema = @Schema(pattern = "^user-[a-zA-Z0-9]{8}$"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check completed",
                    content = @Content(schema = @Schema(
                        implementation = AchievementCheckResponse.class,
                        example = """
                            {
                              "hasAchievement": true,
                              "progress": 0.85,
                              "requirementsMissing": ["tasksCompleted"]
                            }"""))),
        @ApiResponse(responseCode = "404", description = "User or achievement not found")
    })
    public ResponseEntity<AchievementCheckResponse> checkUserAchievement(
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
    @Parameter(name = "userId", example = "user-5566",
              schema = @Schema(pattern = "^user-[a-zA-Z0-9]{8}$"))
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = @Content(examples = {
            @ExampleObject(name = "Task Completed", value = """
                {
                  "eventType": "TASK_COMPLETED",
                  "eventDetails": {
                    "taskId": "task-7788",
                    "category": "DEVELOPMENT",
                    "pointsAwarded": 50
                  }
                }"""),
            @ExampleObject(name = "Milestone Reached", value = """
                {
                  "eventType": "MILESTONE",
                  "eventDetails": {
                    "milestoneType": "STREAK",
                    "daysConsecutive": 30
                  }
                }""")
        }))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Event processed",
                   content = @Content(schema = @Schema(example = """
                       {
                         "success": true,
                         "newAchievements": ["achieve-123", "achieve-456"],
                         "totalAchievements": 5
                       }"""))),
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
     * Award an achievement to a user.
     * 
     * @param achievementId The ID of the achievement to award.
     * @param userId The ID of the user to award the achievement to.
     * @param metadata Additional metadata about the achievement award.
     * @return Success response.
     */
    @PostMapping("/{achievementId}/award/{userId}")
    @Operation(summary = "Award achievement to user", 
              description = "Manually awards an achievement to a user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Awarded",
                   content = @Content(schema = @Schema(example = """
                       {
                         "success": true,
                         "message": "Achievement awarded",
                         "awardDate": "2024-03-20T14:30:00Z"
                       }"""))),
        @ApiResponse(responseCode = "409", description = "Already awarded",
                   content = @Content(schema = @Schema(example = """
                       {
                         "error": "Conflict",
                         "message": "User already has this achievement"
                       }"""))),
        @ApiResponse(responseCode = "404", description = "User or achievement not found")
    })
    public ResponseEntity<ObjectNode> awardAchievement(
        @Parameter(description = "ID of the achievement to award", example = "achieve-123", required = true)
        @PathVariable String achievementId,
        @Parameter(description = "ID of the user to award the achievement to", example = "user-456", required = true)
        @PathVariable String userId,
        @Parameter(description = "Additional metadata about the achievement award")
        @RequestBody(required = false) 
        @Schema(example = """
            {
                "manualReason": "Special promotion",
                "awardedBy": "admin@example.com"
            }""")
        JsonNode metadata) {
        
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        
        Achievement achievement = achievementService.getAchievementById(achievementId);
        if (achievement == null) {
            throw new AchievementNotFoundException(achievementId);
        }
        
        // Check if the user already has this achievement
        if (userAchievementService.hasAchievement(user, achievement)) {
            ObjectNode resultJson = objectMapper.createObjectNode();
            resultJson.put("success", false);
            resultJson.put("message", "User already has this achievement");
            return ResponseEntity.ok(resultJson);
        }
        
        // Use empty metadata if none provided
        if (metadata == null) {
            metadata = objectMapper.createObjectNode();
            ((ObjectNode) metadata).put("awardedManually", true);
        }
        
        // Award the achievement
        UserAchievement userAchievement = userAchievementService.awardAchievement(user, achievement, metadata);
        
        ObjectNode resultJson = objectMapper.createObjectNode();
        resultJson.put("success", true);
        resultJson.put("message", "Achievement awarded successfully");
        resultJson.put("userId", userId);
        resultJson.put("achievementId", achievementId);
        resultJson.put("achievementName", achievement.getName());
        
        return ResponseEntity.ok(resultJson);
    }
    
    /**
     * Get users who have earned a specific achievement.
     * 
     * @param achievementId The ID of the achievement.
     * @return A list of users who have earned the achievement.
     */
    @GetMapping("/{achievementId}/users")
    @Operation(summary = "Get users with achievement", 
              description = "Retrieves all users who have earned a specific achievement")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved users",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))),
        @ApiResponse(responseCode = "404", description = "Achievement not found")
    })
    public ResponseEntity<List<User>> getAchievementUsers(
        @Parameter(description = "ID of the achievement", example = "achieve-123")
        @PathVariable String achievementId) {
        
        Achievement achievement = achievementService.getAchievementById(achievementId);
        
        List<UserAchievement> userAchievements = userAchievementService.getAchievementUsers(achievement);
        List<User> users = userAchievements.stream()
                .map(UserAchievement::getUser)
                .collect(java.util.stream.Collectors.toList());
        
        return ResponseEntity.ok(users);
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
