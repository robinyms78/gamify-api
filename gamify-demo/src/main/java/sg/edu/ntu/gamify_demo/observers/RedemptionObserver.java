package sg.edu.ntu.gamify_demo.observers;

import sg.edu.ntu.gamify_demo.models.RewardRedemption;

/**
 * Observer interface for redemption events.
 * Implementations of this interface can subscribe to redemption-related events
 * such as creation and status changes.
 */
public interface RedemptionObserver {
    
    /**
     * Called when a new redemption is created.
     * 
     * @param redemption The newly created redemption
     */
    void onRedemptionCreated(RewardRedemption redemption);
    
    /**
     * Called when a redemption's status changes.
     * 
     * @param redemption The redemption whose status changed
     * @param oldStatus The previous status
     * @param newStatus The new status
     */
    void onRedemptionStatusChanged(RewardRedemption redemption, String oldStatus, String newStatus);
}
