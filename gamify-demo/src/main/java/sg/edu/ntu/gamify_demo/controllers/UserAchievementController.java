package sg.edu.ntu.gamify_demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sg.edu.ntu.gamify_demo.dtos.UserAchievementDTO;
import sg.edu.ntu.gamify_demo.exceptions.UserNotFoundException;
import sg.edu.ntu.gamify_demo.facades.GamificationFacade;
import sg.edu.ntu.gamify_demo.interfaces.AchievementService;
import sg.edu.ntu.gamify_demo.interfaces.UserAchievementService;
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.models.Achievement;
import sg.edu.ntu.gamify_demo.models.User;

/**
 * REST controller for user achievement-related endpoints.
 */
@RestController
@RequestMapping("/api/users")
public class UserAchievementController {
    
    private final UserService userService;
    private final AchievementService achievementService;
    private final UserAchievementService userAchievementService;
    private final GamificationFacade gamificationFacade;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor for dependency injection.
     */
    @Autowired
    public UserAchievementController(
            UserService userService,
            AchievementService achievementService,
            UserAchievementService userAchievementService,
            GamificationFacade gamificationFacade,
            ObjectMapper objectMapper) {
        this.userService = userService;
        this.achievementService = achievementService;
        this.userAchievementService = userAchievementService;
        this.gamificationFacade = gamificationFacade;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Get all achievements earned by a user.
     * 
     * @param userId The ID of the user.
     * @return A list of achievements earned by the user.
     */
    @GetMapping("/{userId}/achievements")
    public ResponseEntity<UserAchievementDTO> getUserAchievements(@PathVariable String userId) {
        UserAchievementDTO userAchievements = gamificationFacade.getUserAchievements(userId);
        return ResponseEntity.ok(userAchievements);
    }
    
    /**
     * Get the count of achievements earned by a user.
     * 
     * @param userId The ID of the user.
     * @return The number of achievements earned by the user.
     */
    @GetMapping("/{userId}/achievements/count")
    public ResponseEntity<ObjectNode> getUserAchievementCount(@PathVariable String userId) {
        User user = userService.getUserById(userId);
        
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        
        long count = userAchievementService.countUserAchievements(user);
        
        ObjectNode result = objectMapper.createObjectNode();
        result.put("userId", userId);
        result.put("username", user.getUsername());
        result.put("achievementCount", count);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Check if a user has a specific achievement.
     * 
     * @param userId The ID of the user.
     * @param achievementId The ID of the achievement.
     * @return True if the user has the achievement, false otherwise.
     */
    @GetMapping("/{userId}/achievements/{achievementId}")
    public ResponseEntity<ObjectNode> hasUserAchievement(
            @PathVariable String userId,
            @PathVariable String achievementId) {
        
        User user = userService.getUserById(userId);
        
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        
        Achievement achievement = achievementService.getAchievementById(achievementId);
        
        boolean hasAchievement = userAchievementService.hasAchievement(user, achievement);
        
        ObjectNode result = objectMapper.createObjectNode();
        result.put("userId", userId);
        result.put("username", user.getUsername());
        result.put("achievementId", achievementId);
        result.put("achievementName", achievement.getName());
        result.put("hasAchievement", hasAchievement);
        
        return ResponseEntity.ok(result);
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
        
        return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND).body(errorJson);
    }
}
