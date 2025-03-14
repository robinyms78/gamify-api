package sg.edu.ntu.gamify_demo.interfaces;

import java.util.List;

import sg.edu.ntu.gamify_demo.dtos.RedemptionResult;
import sg.edu.ntu.gamify_demo.models.RewardRedemption;

/**
 * Service interface for reward redemption operations.
 * This interface defines methods for managing reward redemptions,
 * including creating, retrieving, updating, and processing redemptions.
 */
public interface RewardRedemptionService {
    /**
     * Gets all redemptions.
     * 
     * @return A list of all redemptions
     */
    List<RewardRedemption> getAllRedemptions();

    /**
     * Saves a redemption.
     * 
     * @param redemption The redemption to save
     * @return The saved redemption
     */
    RewardRedemption saveRedemption(RewardRedemption redemption);

    /**
     * Gets a redemption by ID.
     * 
     * @param redemptionId The ID of the redemption to get
     * @return The redemption
     */
    RewardRedemption getRedemption(String redemptionId);

    /**
     * Updates a redemption.
     * 
     * @param redemptionId The ID of the redemption to update
     * @param redemption The updated redemption data
     * @return The updated redemption
     */
    RewardRedemption updateRedemption(String redemptionId, RewardRedemption redemption);
    
    /**
     * Deletes a redemption.
     * 
     * @param redemptionId The ID of the redemption to delete
     */
    void deleteRedemption(String redemptionId);

    /**
     * Redeems a reward for a user.
     * 
     * @param userId The ID of the user redeeming the reward
     * @param rewardId The ID of the reward being redeemed
     * @return A RedemptionResult containing the result of the redemption
     */
    RedemptionResult redeemReward(String userId, String rewardId);
    
    /**
     * Completes a redemption.
     * This method transitions a redemption from PROCESSING to COMPLETED status.
     * 
     * @param redemptionId The ID of the redemption to complete
     * @return A RedemptionResult containing the result of the operation
     */
    RedemptionResult completeRedemption(String redemptionId);
    
    /**
     * Cancels a redemption.
     * This method transitions a redemption to CANCELLED status.
     * 
     * @param redemptionId The ID of the redemption to cancel
     * @return A RedemptionResult containing the result of the operation
     */
    RedemptionResult cancelRedemption(String redemptionId);

    /**
     * Counts the total number of redemptions.
     * 
     * @return The number of redemptions
     */
    int countRedemptions();
}
