package sg.edu.ntu.gamify_demo.controllers;

import java.util.List;
import java.util.stream.Collectors;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import sg.edu.ntu.gamify_demo.exceptions.UserNotFoundException;
import sg.edu.ntu.gamify_demo.interfaces.AchievementService;
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.models.Achievement;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.UserAchievement;
import sg.edu.ntu.gamify_demo.services.GamificationService;
import sg.edu.ntu.gamify_demo.services.LadderService;

/**
 * REST controller for gamification-related endpoints.
 */
@RestController
@RequestMapping("/api/gamification")
@Tag(name = "Gamification", description = "Points and achievements management")
public class GamificationController {
    
    @Autowired
    private GamificationService gamificationService;
    
    @Autowired
    private AchievementService achievementService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private LadderService ladderService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Get a user's points.
     * 
     * @param userId The ID of the user.
     * @return The user's points.
     */
    @GetMapping("/users/{userId}/points")
    @Operation(summary = "Get user points", description = "Retrieves total points for a user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved points")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<ObjectNode> getUserPoints(
        @Parameter(description = "User ID", example = "uuid-1234") 
        @PathVariable String userId) {
        int points = gamificationService.getUserPoints(userId);
        
        ObjectNode result = objectMapper.createObjectNode();
        result.put("userId", userId);
        result.put("points", points);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Award points to a user.
     * 
     * @param userId The ID of the user.
     * @param pointsData JSON data containing points, eventType, and eventData.
     * @return The user's new total points.
     */
    @PostMapping("/users/{userId}/points/award")
    public ResponseEntity<ObjectNode> awardPoints(
            @PathVariable String userId,
            @RequestBody JsonNode pointsData) {
        
        int points = pointsData.get("points").asInt();
        String eventType = pointsData.get("eventType").asText();
        JsonNode eventData = pointsData.get("eventData");
        
        int newPoints = gamificationService.awardPoints(userId, points, eventType, eventData);
        
        // Update the user's ladder status
        ladderService.updateUserLadderStatus(userId);
        
        ObjectNode result = objectMapper.createObjectNode();
        result.put("userId", userId);
        result.put("pointsAwarded", points);
        result.put("newTotal", newPoints);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Spend points from a user's available points.
     * 
     * @param userId The ID of the user.
     * @param pointsData JSON data containing points, eventType, and eventData.
     * @return Success or failure message.
     */
    @PostMapping("/users/{userId}/points/spend")
    public ResponseEntity<ObjectNode> spendPoints(
            @PathVariable String userId,
            @RequestBody JsonNode pointsData) {
        
        int points = pointsData.get("points").asInt();
        String eventType = pointsData.get("eventType").asText();
        JsonNode eventData = pointsData.get("eventData");
        
        boolean success = gamificationService.spendPoints(userId, points, eventType, eventData);
        
        ObjectNode result = objectMapper.createObjectNode();
        result.put("userId", userId);
        result.put("pointsSpent", points);
        result.put("success", success);
        
        if (!success) {
            result.put("message", "Not enough available points");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Process achievements for a user based on an event.
     * 
     * @param userId The ID of the user.
     * @param eventData JSON data containing eventType and eventDetails.
     * @return A list of newly awarded achievements.
     */
    @PostMapping("/users/{userId}/achievements/process")
    public ResponseEntity<List<Achievement>> processAchievements(
            @PathVariable String userId,
            @RequestBody JsonNode eventData) {
        
        User user = userService.getUserById(userId);
        
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        
        String eventType = eventData.get("eventType").asText();
        JsonNode eventDetails = eventData.get("eventDetails");
        
        List<UserAchievement> newUserAchievements = gamificationService.processAchievements(user, eventType, eventDetails);
        
        List<Achievement> newAchievements = newUserAchievements.stream()
                .map(UserAchievement::getAchievement)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(newAchievements);
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
