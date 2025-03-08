package sg.edu.ntu.gamify_demo.controllers;

import java.util.List;
import java.util.stream.Collectors;

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

import sg.edu.ntu.gamify_demo.exceptions.AchievementNotFoundException;
import sg.edu.ntu.gamify_demo.exceptions.UserNotFoundException;
import sg.edu.ntu.gamify_demo.interfaces.AchievementService;
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.models.Achievement;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.UserAchievement;

/**
 * REST controller for achievement-related endpoints.
 */
@RestController
@RequestMapping("/api/achievements")
public class AchievementController {
    
    @Autowired
    private AchievementService achievementService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Get all achievements.
     * 
     * @return A list of all achievements.
     */
    @GetMapping
    public ResponseEntity<List<Achievement>> getAllAchievements() {
        List<Achievement> achievements = achievementService.getAllAchievements();
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
     * Get all users who have earned a specific achievement.
     * 
     * @param achievementId The ID of the achievement.
     * @return A list of users who have earned the achievement.
     */
    @GetMapping("/{achievementId}/users")
    public ResponseEntity<List<User>> getAchievementUsers(@PathVariable String achievementId) {
        Achievement achievement = achievementService.getAchievementById(achievementId);
        List<UserAchievement> userAchievements = achievementService.getAchievementUsers(achievement);
        
        List<User> users = userAchievements.stream()
                .map(UserAchievement::getUser)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(users);
    }
    
    /**
     * Award an achievement to a user.
     * 
     * @param achievementId The ID of the achievement.
     * @param userId The ID of the user.
     * @param metadata Optional metadata about how the achievement was earned.
     * @return The created UserAchievement.
     */
    @PostMapping("/{achievementId}/award/{userId}")
    public ResponseEntity<UserAchievement> awardAchievement(
            @PathVariable String achievementId,
            @PathVariable String userId,
            @RequestBody(required = false) JsonNode metadata) {
        
        Achievement achievement = achievementService.getAchievementById(achievementId);
        User user = userService.getUserById(userId);
        
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        
        // If no metadata is provided, create an empty object
        if (metadata == null) {
            metadata = objectMapper.createObjectNode();
        }
        
        UserAchievement userAchievement = achievementService.awardAchievement(user, achievement, metadata);
        
        if (userAchievement == null) {
            // User already has this achievement
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        return new ResponseEntity<>(userAchievement, HttpStatus.CREATED);
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
        
        boolean hasAchievement = achievementService.hasAchievement(user, achievement);
        
        ObjectNode result = objectMapper.createObjectNode();
        result.put("hasAchievement", hasAchievement);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Process achievements for a user based on an event.
     * 
     * @param userId The ID of the user.
     * @param eventData JSON data containing eventType and additional event data.
     * @return A list of newly awarded achievements.
     */
    @PostMapping("/process/{userId}")
    public ResponseEntity<List<Achievement>> processAchievements(
            @PathVariable String userId,
            @RequestBody JsonNode eventData) {
        
        User user = userService.getUserById(userId);
        
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        
        String eventType = eventData.get("eventType").asText();
        JsonNode eventDetails = eventData.get("eventDetails");
        
        List<UserAchievement> newUserAchievements = achievementService.processAchievements(user, eventType, eventDetails);
        
        List<Achievement> newAchievements = newUserAchievements.stream()
                .map(UserAchievement::getAchievement)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(newAchievements);
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
