package sg.edu.ntu.gamify_demo.controllers;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.UserLadderStatus;
import sg.edu.ntu.gamify_demo.repositories.UserLadderStatusRepository;
import sg.edu.ntu.gamify_demo.services.LadderService;

/**
 * Debug controller for diagnosing issues with the ladder status system.
 * This controller is intended for development and testing purposes only.
 */
@RestController
@RequestMapping("/debug")
public class DebugController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private LadderService ladderService;
    
    @Autowired
    private UserLadderStatusRepository userLadderStatusRepository;
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * Debug endpoint to get database information.
     * 
     * @return A map of database information.
     */
    @GetMapping("/db-info")
    public Map<String, Object> getDatabaseInfo() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String dbProductName = jdbcTemplate.queryForObject("SELECT current_setting('server_version')", String.class);
            result.put("Database", "PostgreSQL " + dbProductName);
            
            String dbUrl = dataSource.getConnection().getMetaData().getURL();
            result.put("URL", dbUrl);
            
            String dbUser = dataSource.getConnection().getMetaData().getUserName();
            result.put("User", dbUser);
            
            result.put("Status", "Connected");
        } catch (Exception e) {
            result.put("Error", e.getMessage());
            result.put("Status", "Error");
        }
        
        return result;
    }
    
    /**
     * Debug endpoint for ladder status issues.
     * 
     * @param userId The ID of the user to debug.
     * @return A map of debug information.
     */
    @GetMapping("/ladder-status/{userId}")
    public Map<String, Object> debugLadderStatus(@PathVariable String userId) {
        Map<String, Object> result = new HashMap<>();
        
        // Get user
        User user = userService.getUserById(userId);
        result.put("userExists", user != null);
        if (user != null) {
            result.put("userId", user.getId());
            result.put("userEarnedPoints", user.getEarnedPoints());
        }
        
        // Check if ladder status exists
        boolean statusExists = userLadderStatusRepository.existsById(userId);
        result.put("ladderStatusExists", statusExists);
        
        // Try to get ladder status
        try {
            UserLadderStatus status = ladderService.getUserLadderStatus(userId);
            result.put("ladderStatusRetrieved", status != null);
            if (status != null) {
                result.put("ladderStatusId", status.getId());
                result.put("ladderStatusUserId", status.getUser() != null ? status.getUser().getId() : null);
                result.put("ladderStatusCurrentLevel", status.getCurrentLevel() != null ? status.getCurrentLevel().getLevel() : null);
                result.put("ladderStatusEarnedPoints", status.getEarnedPoints());
                result.put("ladderStatusPointsToNextLevel", status.getPointsToNextLevel());
            }
        } catch (Exception e) {
            result.put("ladderStatusError", e.getMessage());
            result.put("ladderStatusErrorType", e.getClass().getName());
            
            // Try to recover by initializing
            if (user != null) {
                try {
                    result.put("attemptingRecovery", true);
                    UserLadderStatus status = ladderService.initializeUserLadderStatus(user);
                    result.put("recoverySuccessful", status != null);
                    if (status != null) {
                        result.put("recoveredLadderStatusId", status.getId());
                    }
                } catch (Exception recoveryEx) {
                    result.put("recoveryError", recoveryEx.getMessage());
                    result.put("recoveryErrorType", recoveryEx.getClass().getName());
                }
            }
        }
        
        return result;
    }
    
    /**
     * Force initialization of ladder status for a user.
     * 
     * @param userId The ID of the user.
     * @return A map of initialization results.
     */
    @GetMapping("/ladder-status/{userId}/initialize")
    public Map<String, Object> forceInitializeLadderStatus(@PathVariable String userId) {
        Map<String, Object> result = new HashMap<>();
        
        // Get user
        User user = userService.getUserById(userId);
        result.put("userExists", user != null);
        
        if (user == null) {
            result.put("error", "User not found");
            return result;
        }
        
        // Force initialization
        try {
            UserLadderStatus status = ladderService.initializeUserLadderStatus(user);
            result.put("initializationSuccessful", status != null);
            if (status != null) {
                result.put("ladderStatusId", status.getId());
                result.put("ladderStatusUserId", status.getUser() != null ? status.getUser().getId() : null);
                result.put("ladderStatusCurrentLevel", status.getCurrentLevel() != null ? status.getCurrentLevel().getLevel() : null);
                result.put("ladderStatusEarnedPoints", status.getEarnedPoints());
                result.put("ladderStatusPointsToNextLevel", status.getPointsToNextLevel());
            }
        } catch (Exception e) {
            result.put("initializationError", e.getMessage());
            result.put("initializationErrorType", e.getClass().getName());
            result.put("stackTrace", e.getStackTrace());
        }
        
        return result;
    }
}
