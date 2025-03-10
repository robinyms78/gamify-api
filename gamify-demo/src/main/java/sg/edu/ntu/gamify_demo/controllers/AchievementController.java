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
    @Operation(summary = "Retrieve all achievements")
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
    public ResponseEntity<Achievement> getAchievementById(@PathVariable String achievementId) {
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
    public ResponseEntity<Achievement> createAchievement(@RequestBody JsonNode achievementData) {
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
    public ResponseEntity<Achievement> updateAchievement(
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
    public ResponseEntity<Void> deleteAchievement(@PathVariable String achievementId) {
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
    public ResponseEntity<UserAchievementDTO> getUserAchievements(@PathVariable String userId) {
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
    public ResponseEntity<ObjectNode> checkUserAchievement(
            @PathVariable String achievementId,
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
    public ResponseEntity<ObjectNode> processEvent(
            @PathVariable String userId,
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
