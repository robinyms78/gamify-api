package sg.edu.ntu.gamify_demo.models.enums;

/**
 * Enum representing the possible statuses of a reward redemption.
 */
public enum RedemptionStatus {
    /**
     * The redemption request has been received and is being processed.
     */
    PROCESSING,
    
    /**
     * The redemption has been completed successfully.
     */
    COMPLETED,
    
    /**
     * The redemption has been rejected or failed.
     */
    FAILED,
    
    /**
     * The redemption has been cancelled by the user.
     */
    CANCELLED
}
