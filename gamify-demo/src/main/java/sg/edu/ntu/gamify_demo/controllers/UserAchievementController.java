package sg.edu.ntu.gamify_demo.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User Achievements", description = "Endpoints for managing user achievement tracking")
public class UserAchievementController {
    
    private final UserService userService;
    private final AchievementService achievementService;
    private final UserAchievementService userAchievementService;
    private final GamificationFacade gamificationFacade;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor for dependency injection.
     */
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
    @Operation(summary = "Get user achievements", 
              description = "Retrieves all achievements earned by a user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved achievements",
                    content = @Content(schema = @Schema(implementation = UserAchievementDTO.class))),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserAchievementDTO> getUserAchievements(
        @Parameter(description = "ID of the user", required = true, example = "user-12345")
        @PathVariable String userId) {
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
    @Operation(summary = "Get achievement count", 
              description = "Retrieves the total number of achievements earned by a user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved count",
                    content = @Content(schema = @Schema(implementation = ObjectNode.class))),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ObjectNode> getUserAchievementCount(
        @Parameter(description = "ID of the user", required = true, example = "user-67890")
        @PathVariable String userId) {
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
    @Operation(summary = "Check achievement status", 
              description = "Checks if a user has earned a specific achievement")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check completed successfully",
                    content = @Content(schema = @Schema(implementation = ObjectNode.class))),
        @ApiResponse(responseCode = "404", description = "User or achievement not found")
    })
    public ResponseEntity<ObjectNode> hasUserAchievement(
        @Parameter(description = "ID of the user", required = true, example = "user-45678")
        @PathVariable String userId,
        @Parameter(description = "ID of the achievement", required = true, example = "achieve-1122")
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
