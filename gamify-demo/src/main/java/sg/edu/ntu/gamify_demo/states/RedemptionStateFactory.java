package sg.edu.ntu.gamify_demo.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sg.edu.ntu.gamify_demo.models.RewardRedemption;
import sg.edu.ntu.gamify_demo.models.enums.RedemptionStatus;

/**
 * Factory for creating RedemptionState objects based on redemption status.
 * This factory implements the Factory pattern to create the appropriate state
 * for a given redemption status.
 */
@Component
public class RedemptionStateFactory {
    
    @Autowired
    private ProcessingState processingState;
    
    /**
     * Gets the appropriate state for a redemption based on its status.
     * 
     * @param redemption The redemption to get the state for
     * @return The appropriate RedemptionState
     */
    public RedemptionState getStateForRedemption(RewardRedemption redemption) {
        if (redemption == null || redemption.getStatus() == null) {
            return processingState; // Default to processing state
        }
        
        try {
            RedemptionStatus status = RedemptionStatus.valueOf(redemption.getStatus());
            
            switch (status) {
                case PROCESSING:
                    return processingState;
                // Add other states as they are implemented
                default:
                    return processingState;
            }
        } catch (IllegalArgumentException e) {
            // If status is not a valid enum value, default to processing
            return processingState;
        }
    }
    
    /**
     * Gets the appropriate state for a given status.
     * 
     * @param status The status to get the state for
     * @return The appropriate RedemptionState
     */
    public RedemptionState getStateForStatus(RedemptionStatus status) {
        if (status == null) {
            return processingState; // Default to processing state
        }
        
        switch (status) {
            case PROCESSING:
                return processingState;
            // Add other states as they are implemented
            default:
                return processingState;
        }
    }
}
