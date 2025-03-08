package sg.edu.ntu.gamify_demo.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sg.edu.ntu.gamify_demo.exceptions.UserNotFoundException;
import sg.edu.ntu.gamify_demo.interfaces.AchievementService;
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.models.Achievement;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.UserAchievement;

/**
 * REST controller for user achievement-related endpoints.
 */
@RestController
@RequestMapping("/api/users")
public class UserAchievementController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AchievementService achievementService;
    
    /**
     * Get all achievements earned by a user.
     * 
     * @param userId The ID of the user.
     * @return A list of achievements earned by the user.
     */
    @GetMapping("/{userId}/achievements")
    public ResponseEntity<List<Achievement>> getUserAchievements(@PathVariable String userId) {
        User user = userService.getUserById(userId);
        
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        
        List<UserAchievement> userAchievements = achievementService.getUserAchievements(user);
        
        List<Achievement> achievements = userAchievements.stream()
                .map(UserAchievement::getAchievement)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(achievements);
    }
    
    /**
     * Get the count of achievements earned by a user.
     * 
     * @param userId The ID of the user.
     * @return The number of achievements earned by the user.
     */
    @GetMapping("/{userId}/achievements/count")
    public ResponseEntity<Long> getUserAchievementCount(@PathVariable String userId) {
        User user = userService.getUserById(userId);
        
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        
        long count = achievementService.countUserAchievements(user);
        
        return ResponseEntity.ok(count);
    }
    
    /**
     * Check if a user has a specific achievement.
     * 
     * @param userId The ID of the user.
     * @param achievementId The ID of the achievement.
     * @return True if the user has the achievement, false otherwise.
     */
    @GetMapping("/{userId}/achievements/{achievementId}")
    public ResponseEntity<Boolean> hasUserAchievement(
            @PathVariable String userId,
            @PathVariable String achievementId) {
        
        User user = userService.getUserById(userId);
        
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        
        Achievement achievement = achievementService.getAchievementById(achievementId);
        
        boolean hasAchievement = achievementService.hasAchievement(user, achievement);
        
        return ResponseEntity.ok(hasAchievement);
    }
}
