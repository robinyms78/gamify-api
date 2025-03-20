package sg.edu.ntu.gamify_demo.observers;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import sg.edu.ntu.gamify_demo.models.RewardRedemption;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.enums.RedemptionStatus;
import sg.edu.ntu.gamify_demo.services.PointsTransactionService;

/**
 * Observer implementation that refunds points when a redemption is cancelled.
 * This class is responsible for monitoring redemption status changes and
 * refunding points to the user when a redemption is cancelled.
 */
@Component
@RequiredArgsConstructor
public class PointsRefundObserver implements RedemptionObserver {
    
    private final PointsTransactionService pointsTransactionService;
    private final ObjectMapper objectMapper;
    
    @Override
    public void onRedemptionCreated(RewardRedemption redemption) {
        // No action needed when a redemption is created
    }
    
    @Override
    public void onRedemptionStatusChanged(RewardRedemption redemption, String oldStatus, String newStatus) {
        // Only process if the new status is CANCELLED
        if (RedemptionStatus.CANCELLED.name().equals(newStatus)) {
            User user = redemption.getUser();
            Long pointsToRefund = redemption.getReward().getCostInPoints();
            
            // Create metadata for the refund transaction
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("redemptionId", redemption.getId());
            metadata.put("rewardId", redemption.getReward().getId());
            metadata.put("rewardName", redemption.getReward().getName());
            metadata.put("reason", "REDEMPTION_CANCELLED");
            
            // Record the points refund
            pointsTransactionService.recordPointsEarned(
                user,
                pointsToRefund,
                "REDEMPTION_REFUND",
                metadata
            );
        }
    }
}
