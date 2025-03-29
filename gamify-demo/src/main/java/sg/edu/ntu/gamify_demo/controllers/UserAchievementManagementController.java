package sg.edu.ntu.gamify_demo.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import sg.edu.ntu.gamify_demo.exceptions.AchievementNotFoundException;
import sg.edu.ntu.gamify_demo.exceptions.UserNotFoundException;
import sg.edu.ntu.gamify_demo.interfaces.AchievementService;
import sg.edu.ntu.gamify_demo.interfaces.UserAchievementService;
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.models.Achievement;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.UserAchievement;

/**
 * REST controller for managing user achievements.
 */
@RestController
@RequestMapping("/api/user-achievements")
@Tag(name = "User Achievement Management", description = "Endpoints for managing user achievements")
public class UserAchievementManagementController {
    
    private final UserService userService;
    private final AchievementService achievementService;
    private final UserAchievementService userAchievementService;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor for dependency injection.
     */
    public UserAchievementManagementController(
            UserService userService,
            AchievementService achievementService,
            UserAchievementService userAchievementService,
            ObjectMapper objectMapper) {
        this.userService = userService;
        this.achievementService = achievementService;
        this.userAchievementService = userAchievementService;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Award an achievement to a user.
     * 
     * @param achievementId The ID of the achievement to award.
     * @param userId The ID of the user to award the achievement to.
     * @param metadata Additional metadata about the achievement award.
     * @return Success response.
     */
    @PostMapping("/award")
    @Operation(summary = "Award achievement to user", 
              description = "Manually awards an achievement to a user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Achievement awarded successfully",
                    content = @Content(schema = @Schema(implementation = ObjectNode.class))),
        @ApiResponse(responseCode = "404", description = "User or achievement not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<ObjectNode> awardAchievement(
        @Parameter(description = "Achievement ID", required = true)
        @RequestBody JsonNode requestBody) {
        
        if (!requestBody.has("achievementId") || !requestBody.has("userId")) {
            ObjectNode errorJson = objectMapper.createObjectNode();
            errorJson.put("error", "Bad request");
            errorJson.put("message", "achievementId and userId are required");
            return ResponseEntity.badRequest().body(errorJson);
        }
        
        String achievementId = requestBody.get("achievementId").asText();
        String userId = requestBody.get("userId").asText();
        JsonNode metadata = requestBody.has("metadata") ? requestBody.get("metadata") : objectMapper.createObjectNode();
        
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
     * Award an achievement to a user (alternative endpoint with path variables).
     * 
     * @param achievementId The ID of the achievement to award.
     * @param userId The ID of the user to award the achievement to.
     * @param metadata Additional metadata about the achievement award.
     * @return Success response.
     */
    @PostMapping("/{achievementId}/award/{userId}")
    @Operation(summary = "Award achievement to user (alternative)", 
              description = "Manually awards an achievement to a user using path variables")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Achievement awarded successfully",
                    content = @Content(schema = @Schema(implementation = ObjectNode.class))),
        @ApiResponse(responseCode = "404", description = "User or achievement not found")
    })
    public ResponseEntity<ObjectNode> awardAchievementAlt(
        @Parameter(description = "ID of the achievement to award", required = true)
        @PathVariable String achievementId,
        @Parameter(description = "ID of the user to award the achievement to", required = true)
        @PathVariable String userId,
        @Parameter(description = "Additional metadata about the achievement award")
        @RequestBody(required = false) JsonNode metadata) {
        
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
     * Award an achievement to a user using query parameters.
     * 
     * @param achievementId The ID of the achievement to award.
     * @param userId The ID of the user to award the achievement to.
     * @param metadata Additional metadata about the achievement award.
     * @return Success response.
     */
    @PostMapping("/award-with-params")
    @Operation(summary = "Award achievement to user using query parameters", 
              description = "Manually awards an achievement to a user using query parameters")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Achievement awarded successfully",
                    content = @Content(schema = @Schema(implementation = ObjectNode.class))),
        @ApiResponse(responseCode = "404", description = "User or achievement not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<ObjectNode> awardAchievementWithParams(
        @Parameter(description = "Achievement ID", required = true)
        @RequestParam(required = false) String achievementId,
        @Parameter(description = "User ID", required = true)
        @RequestParam(required = false) String userId,
        @Parameter(description = "Additional metadata about the achievement award")
        @RequestBody(required = false) JsonNode metadata) {
        
        if (achievementId == null || userId == null) {
            ObjectNode errorJson = objectMapper.createObjectNode();
            errorJson.put("error", "Bad request");
            errorJson.put("message", "achievementId and userId are required");
            return ResponseEntity.badRequest().body(errorJson);
        }
        
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
     * Award an achievement to a user (endpoint for compatibility with test script).
     * This endpoint is specifically added to match the endpoint used in the test script.
     * 
     * @param achievementId The ID of the achievement to award.
     * @param userId The ID of the user to award the achievement to.
     * @param metadata Additional metadata about the achievement award.
     * @return Success response.
     */
    @PostMapping(value = {"/achievements/{achievementId}/award/{userId}", "/api/achievements/{achievementId}/award/{userId}"}, produces = "application/json")
    @Operation(summary = "Award achievement to user (test script compatibility)", 
              description = "Manually awards an achievement to a user (compatibility endpoint)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Achievement awarded successfully",
                    content = @Content(schema = @Schema(implementation = ObjectNode.class))),
        @ApiResponse(responseCode = "404", description = "User or achievement not found")
    })
    public ResponseEntity<ObjectNode> awardAchievementForTestScript(
        @Parameter(description = "ID of the achievement to award", required = true)
        @PathVariable String achievementId,
        @Parameter(description = "ID of the user to award the achievement to", required = true)
        @PathVariable String userId,
        @Parameter(description = "Additional metadata about the achievement award")
        @RequestBody(required = false) JsonNode metadata) {
        
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
