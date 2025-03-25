package sg.edu.ntu.gamify_demo.controllers;

import java.time.ZonedDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import sg.edu.ntu.gamify_demo.dtos.RedemptionResult;
import sg.edu.ntu.gamify_demo.dtos.RewardRedemptionRequestDTO;
import sg.edu.ntu.gamify_demo.interfaces.RewardRedemptionService;
import sg.edu.ntu.gamify_demo.interfaces.RewardService;
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.models.Rewards;
import sg.edu.ntu.gamify_demo.models.User;

/**
 * Controller for test-only endpoints.
 * These endpoints are only for testing purposes and should not be used in production.
 */
@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RewardRedemptionService rewardRedemptionService;
    
    @Autowired
    private RewardService rewardService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Directly update a user's points for testing purposes.
     * This bypasses the normal flow that would update the user's ladder status.
     * 
     * @param userId The ID of the user.
     * @param points The new points value.
     * @return Success or failure message.
     */
    @PostMapping("/users/{userId}/points")
    public ResponseEntity<ObjectNode> updateUserPoints(
            @PathVariable String userId,
            @RequestParam Long points) {
        
        User user = userService.getUserById(userId);
        
        if (user == null) {
            ObjectNode errorJson = objectMapper.createObjectNode();
            errorJson.put("error", "User not found");
            errorJson.put("message", "User with ID " + userId + " not found");
            return ResponseEntity.notFound().build();
        }
        
        // Directly update the user's points
        user.setEarnedPoints(points);
        user.setAvailablePoints(points);
        userService.updateUser(user.getId(), user);
        
        ObjectNode result = objectMapper.createObjectNode();
        result.put("userId", userId);
        result.put("points", points);
        result.put("success", true);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Directly redeem a reward for testing purposes.
     * This bypasses the normal flow that might cause JSON parsing issues.
     * 
     * @param userId The ID of the user.
     * @param rewardId The ID of the reward.
     * @return The result of the redemption.
     */
    @PostMapping("/redeem")
    public ResponseEntity<?> redeemReward(
            @RequestParam String userId,
            @RequestParam String rewardId) {
        
        try {
            // Get the user to check points
            User user = userService.getUserById(userId);
            if (user == null) {
                ObjectNode errorJson = objectMapper.createObjectNode();
                errorJson.put("error", "User not found");
                errorJson.put("success", false);
                return ResponseEntity.badRequest().body(errorJson);
            }
            
            // Get the reward to check cost
            Rewards reward = rewardService.getReward(rewardId);
            if (reward == null) {
                ObjectNode errorJson = objectMapper.createObjectNode();
                errorJson.put("error", "Reward not found");
                errorJson.put("success", false);
                return ResponseEntity.badRequest().body(errorJson);
            }
            
            // Check if user has enough points
            if (user.getAvailablePoints() < reward.getCostInPoints()) {
                RedemptionResult insufficientResult = RedemptionResult.builder()
                    .success(false)
                    .message("Insufficient points")
                    .updatedPointsBalance(user.getAvailablePoints())
                    .timestamp(ZonedDateTime.now())
                    .build();
                return ResponseEntity.ok(insufficientResult);
            }
            
            // Call the service to redeem the reward
            RedemptionResult result = rewardRedemptionService.redeemReward(userId, rewardId);
            
            // Refresh user from database to get updated points
            user = userService.getUserById(userId);
            
            // Update the result with the latest points balance
            if (result.isSuccess()) {
                result = RedemptionResult.builder()
                    .success(result.isSuccess())
                    .message(result.getMessage())
                    .redemptionId(result.getRedemptionId())
                    .updatedPointsBalance(user.getAvailablePoints())
                    .timestamp(result.getTimestamp())
                    .build();
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            ObjectNode errorJson = objectMapper.createObjectNode();
            errorJson.put("error", "Redemption failed");
            errorJson.put("message", e.getMessage());
            errorJson.put("success", false);
            return ResponseEntity.badRequest().body(errorJson);
        }
    }
    
    /**
     * Directly complete a redemption for testing purposes.
     * 
     * @param redemptionId The ID of the redemption to complete.
     * @return The result of the operation.
     */
    @PostMapping("/redemption/{redemptionId}/complete")
    public ResponseEntity<?> completeRedemption(
            @PathVariable String redemptionId) {
        
        try {
            RedemptionResult result = rewardRedemptionService.completeRedemption(redemptionId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            ObjectNode errorJson = objectMapper.createObjectNode();
            errorJson.put("error", "Completion failed");
            errorJson.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorJson);
        }
    }
    
    /**
     * Directly cancel a redemption for testing purposes.
     * 
     * @param redemptionId The ID of the redemption to cancel.
     * @return The result of the operation.
     */
    @PostMapping("/redemption/{redemptionId}/cancel")
    public ResponseEntity<?> cancelRedemption(
            @PathVariable String redemptionId) {
        
        try {
            RedemptionResult result = rewardRedemptionService.cancelRedemption(redemptionId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            ObjectNode errorJson = objectMapper.createObjectNode();
            errorJson.put("error", "Cancellation failed");
            errorJson.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorJson);
        }
    }
}
