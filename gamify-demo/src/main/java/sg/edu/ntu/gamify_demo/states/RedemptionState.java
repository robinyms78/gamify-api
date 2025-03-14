package sg.edu.ntu.gamify_demo.states;

import sg.edu.ntu.gamify_demo.models.RewardRedemption;

/**
 * Interface for the State pattern implementation of redemption status.
 * Each concrete state handles the behavior for a specific redemption status.
 */
public interface RedemptionState {
    
    /**
     * Process the redemption.
     * 
     * @param redemption The redemption to process
     */
    void process(RewardRedemption redemption);
    
    /**
     * Complete the redemption.
     * 
     * @param redemption The redemption to complete
     */
    void complete(RewardRedemption redemption);
    
    /**
     * Fail the redemption.
     * 
     * @param redemption The redemption that failed
     * @param reason The reason for failure
     */
    void fail(RewardRedemption redemption, String reason);
    
    /**
     * Cancel the redemption.
     * 
     * @param redemption The redemption to cancel
     */
    void cancel(RewardRedemption redemption);
    
    /**
     * Get the name of this state.
     * 
     * @return The state name
     */
    String getStateName();
}
