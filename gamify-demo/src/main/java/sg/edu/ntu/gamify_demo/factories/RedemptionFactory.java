package sg.edu.ntu.gamify_demo.factories;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;

import sg.edu.ntu.gamify_demo.models.Reward;
import sg.edu.ntu.gamify_demo.models.RewardRedemption;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.enums.RedemptionStatus;

/**
 * Factory for creating RewardRedemption objects.
 * This factory encapsulates the logic for creating redemption records,
 * ensuring consistent initialization and status management.
 */
@Component
public class RedemptionFactory {
    
    /**
     * Creates a new redemption record in PROCESSING status.
     * 
     * @param user The user redeeming the reward
     * @param reward The reward being redeemed
     * @return A new RewardRedemption object
     */
    public RewardRedemption createRedemption(User user, Reward reward) {
        return RewardRedemption.builder()
            .id(UUID.randomUUID().toString())
            .user(user)
            .reward(reward)
            .status(RedemptionStatus.PROCESSING.name())
            .createdAt(ZonedDateTime.now())
            .updatedAt(ZonedDateTime.now())
            .build();
    }
    
    /**
     * Creates a new redemption record with the specified status.
     * 
     * @param user The user redeeming the reward
     * @param reward The reward being redeemed
     * @param status The initial status of the redemption
     * @return A new RewardRedemption object
     */
    public RewardRedemption createRedemption(User user, Reward reward, RedemptionStatus status) {
        return RewardRedemption.builder()
            .id(UUID.randomUUID().toString())
            .user(user)
            .reward(reward)
            .status(status.name())
            .createdAt(ZonedDateTime.now())
            .updatedAt(ZonedDateTime.now())
            .build();
    }
}
