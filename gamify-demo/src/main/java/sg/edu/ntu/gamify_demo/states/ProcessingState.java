package sg.edu.ntu.gamify_demo.states;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import sg.edu.ntu.gamify_demo.models.RewardRedemption;
import sg.edu.ntu.gamify_demo.models.enums.RedemptionStatus;
import sg.edu.ntu.gamify_demo.observers.RedemptionObserver;
import sg.edu.ntu.gamify_demo.repositories.RewardRedemptionRepository;

/**
 * Concrete state implementation for redemptions in PROCESSING status.
 */
@Component
public class ProcessingState implements RedemptionState {
    
    @Autowired
    private RewardRedemptionRepository redemptionRepository;
    
    @Autowired
    private List<RedemptionObserver> observers;
    
    @Override
    public void process(RewardRedemption redemption) {
        // Already in processing state, do nothing
    }
    
    @Override
    public void complete(RewardRedemption redemption) {
        String oldStatus = redemption.getStatus();
        redemption.setStatus(RedemptionStatus.COMPLETED.name());
        redemption.setUpdatedAt(ZonedDateTime.now());
        
        redemptionRepository.save(redemption);
        
        // Notify observers
        notifyStatusChanged(redemption, oldStatus, RedemptionStatus.COMPLETED.name());
    }
    
    @Override
    public void fail(RewardRedemption redemption, String reason) {
        String oldStatus = redemption.getStatus();
        redemption.setStatus(RedemptionStatus.FAILED.name());
        redemption.setUpdatedAt(ZonedDateTime.now());
        
        redemptionRepository.save(redemption);
        
        // Notify observers
        notifyStatusChanged(redemption, oldStatus, RedemptionStatus.FAILED.name());
    }
    
    @Override
    public void cancel(RewardRedemption redemption) {
        String oldStatus = redemption.getStatus();
        redemption.setStatus(RedemptionStatus.CANCELLED.name());
        redemption.setUpdatedAt(ZonedDateTime.now());
        
        redemptionRepository.save(redemption);
        
        // Notify observers
        notifyStatusChanged(redemption, oldStatus, RedemptionStatus.CANCELLED.name());
    }
    
    @Override
    public String getStateName() {
        return RedemptionStatus.PROCESSING.name();
    }
    
    /**
     * Helper method to notify observers of status changes.
     */
    private void notifyStatusChanged(RewardRedemption redemption, String oldStatus, String newStatus) {
        if (observers != null) {
            for (RedemptionObserver observer : observers) {
                observer.onRedemptionStatusChanged(redemption, oldStatus, newStatus);
            }
        }
    }
}
