package sg.edu.ntu.gamify_demo.observers;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import sg.edu.ntu.gamify_demo.models.RewardRedemption;
import sg.edu.ntu.gamify_demo.services.MessageBrokerService;

/**
 * Observer implementation that sends notifications when redemption events occur.
 * This class is responsible for converting redemption events into notifications
 * and sending them through the MessageBrokerService.
 */
@Component
@RequiredArgsConstructor
public class NotificationRedemptionObserver implements RedemptionObserver {
    
    private final MessageBrokerService messageBroker;
    private final ObjectMapper objectMapper;
    
    @Override
    public void onRedemptionCreated(RewardRedemption redemption) {
        ObjectNode notification = objectMapper.createObjectNode();
        notification.put("userId", redemption.getUser().getId());
        notification.put("eventType", "REDEMPTION_CREATED");
        notification.put("redemptionId", redemption.getId());
        notification.put("rewardId", redemption.getReward().getId());
        notification.put("rewardName", redemption.getReward().getName());
        notification.put("pointsSpent", redemption.getReward().getCostInPoints());
        notification.put("updatedBalance", redemption.getUser().getAvailablePoints());
        notification.put("status", redemption.getStatus());
        
        messageBroker.sendNotification("redemptions", notification);
    }
    
    @Override
    public void onRedemptionStatusChanged(RewardRedemption redemption, String oldStatus, String newStatus) {
        ObjectNode notification = objectMapper.createObjectNode();
        notification.put("userId", redemption.getUser().getId());
        notification.put("eventType", "REDEMPTION_STATUS_CHANGED");
        notification.put("redemptionId", redemption.getId());
        notification.put("rewardId", redemption.getReward().getId());
        notification.put("rewardName", redemption.getReward().getName());
        notification.put("oldStatus", oldStatus);
        notification.put("newStatus", newStatus);
        
        messageBroker.sendNotification("redemptions", notification);
    }
}
