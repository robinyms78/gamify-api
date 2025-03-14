package sg.edu.ntu.gamify_demo.commands;

import java.time.ZonedDateTime;

import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Builder;
import sg.edu.ntu.gamify_demo.dtos.RedemptionResult;
import sg.edu.ntu.gamify_demo.factories.RedemptionFactory;
import sg.edu.ntu.gamify_demo.models.Reward;
import sg.edu.ntu.gamify_demo.models.RewardRedemption;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.enums.RedemptionStatus;
import sg.edu.ntu.gamify_demo.repositories.RewardRedemptionRepository;
import sg.edu.ntu.gamify_demo.repositories.RewardRepository;
import sg.edu.ntu.gamify_demo.repositories.UserRepository;
import sg.edu.ntu.gamify_demo.services.MessageBrokerService;
import sg.edu.ntu.gamify_demo.services.PointsTransactionService;

/**
 * Command for redeeming a reward.
 * This class encapsulates the entire reward redemption process,
 * including points verification, deduction, record creation, and notification.
 */
@Builder
public class RedeemRewardCommand {
    private final String userId;
    private final String rewardId;
    private final UserRepository userRepository;
    private final RewardRepository rewardRepository;
    private final RewardRedemptionRepository redemptionRepository;
    private final PointsTransactionService pointsTransactionService;
    private final RedemptionFactory redemptionFactory;
    private final MessageBrokerService messageBroker;
    private final ObjectMapper objectMapper;
    
    /**
     * Executes the reward redemption process.
     * 
     * @return A RedemptionResult containing the result of the redemption
     */
    @Transactional
    public RedemptionResult execute() {
        // Fetch user and reward
        User user = userRepository.findById(userId).orElse(null);
        Reward reward = rewardRepository.findById(rewardId).orElse(null);
        
        // Validate user and reward
        if (user == null) {
            return RedemptionResult.builder()
                .success(false)
                .message("User not found")
                .timestamp(ZonedDateTime.now())
                .build();
        }
        
        if (reward == null) {
            return RedemptionResult.builder()
                .success(false)
                .message("Reward not found")
                .timestamp(ZonedDateTime.now())
                .build();
        }
        
        if (!reward.isAvailable()) {
            return RedemptionResult.builder()
                .success(false)
                .message("Reward is not available")
                .timestamp(ZonedDateTime.now())
                .build();
        }
        
        // Check if user has enough points
        Long rewardCost = reward.getCostInPoints();
        if (user.getAvailablePoints() < rewardCost) {
            return RedemptionResult.builder()
                .success(false)
                .message("Insufficient points")
                .updatedPointsBalance(user.getAvailablePoints())
                .timestamp(ZonedDateTime.now())
                .build();
        }
        
        // Create redemption record
        RewardRedemption redemption = redemptionFactory.createRedemption(user, reward, RedemptionStatus.PROCESSING);
        RewardRedemption savedRedemption = redemptionRepository.save(redemption);
        
        // Deduct points
        ObjectNode metadata = objectMapper.createObjectNode();
        metadata.put("redemptionId", savedRedemption.getId());
        metadata.put("rewardId", reward.getId());
        metadata.put("rewardName", reward.getName());
        
        pointsTransactionService.recordPointsSpent(
            user, 
            rewardCost, 
            "REWARD_REDEMPTION", 
            metadata
        );
        
        // Send redemption notification
        sendRedemptionNotification(user, reward, savedRedemption);
        
        // Return success result
        return RedemptionResult.builder()
            .success(true)
            .message("Reward redeemed successfully")
            .updatedPointsBalance(user.getAvailablePoints())
            .redemptionId(savedRedemption.getId())
            .timestamp(ZonedDateTime.now())
            .build();
    }
    
    /**
     * Sends a notification about the redemption.
     */
    private void sendRedemptionNotification(User user, Reward reward, RewardRedemption redemption) {
        if (messageBroker != null) {
            ObjectNode notification = objectMapper.createObjectNode();
            notification.put("userId", user.getId());
            notification.put("eventType", "REDEMPTION_CREATED");
            notification.put("redemptionId", redemption.getId());
            notification.put("rewardId", reward.getId());
            notification.put("rewardName", reward.getName());
            notification.put("pointsSpent", reward.getCostInPoints());
            notification.put("updatedBalance", user.getAvailablePoints());
            notification.put("status", redemption.getStatus());
            
            messageBroker.sendNotification("redemptions", notification);
        }
    }
}
