package sg.edu.ntu.gamify_demo.services;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import sg.edu.ntu.gamify_demo.commands.RedeemRewardCommand;
import sg.edu.ntu.gamify_demo.dtos.RedemptionResult;
import sg.edu.ntu.gamify_demo.dtos.RewardRedemptionRequestDTO;
import sg.edu.ntu.gamify_demo.factories.RedemptionFactory;
import sg.edu.ntu.gamify_demo.interfaces.RewardRedemptionService;
import sg.edu.ntu.gamify_demo.interfaces.RewardService;
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.models.RewardRedemption;
import sg.edu.ntu.gamify_demo.models.Rewards;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.observers.RedemptionObserver;
import sg.edu.ntu.gamify_demo.repositories.RewardRedemptionRepository;
import sg.edu.ntu.gamify_demo.repositories.RewardRepository;
import sg.edu.ntu.gamify_demo.repositories.UserRepository;
import sg.edu.ntu.gamify_demo.states.RedemptionStateFactory;

/**
 * Implementation of RewardService and RewardRedemptionService.
 * This service handles reward management and redemption operations.
 */
@Service
@Component
@RequiredArgsConstructor
public class RewardServiceWithLoggingImpl implements RewardService, RewardRedemptionService {

    private final UserRepository userRepository;
    private final RewardRepository rewardRepository;
    private final RewardRedemptionRepository rewardRedemptionRepository;
    private final PointsTransactionService pointsTransactionService;
    private final RedemptionFactory redemptionFactory;
    private final RedemptionStateFactory stateFactory;
    private final ObjectMapper objectMapper;
    private final MessageBrokerService messageBroker;
    private final List<RedemptionObserver> observers;
    
    private final Logger logger = LoggerFactory.getLogger(RewardService.class);

    // Method
    // Save reward
    @Override
    public Rewards saveReward(Rewards reward) {
        Rewards newReward = rewardRepository.save(reward);
        logger.info("🟢 RewardServiceWithLoggingImpl.createReward() called");
        return newReward;
    }

    // Get reward by id
    @Override
    public Rewards getReward(String rewardId) {
        Rewards reward = rewardRepository.findById(rewardId).get();
        logger.info("🟢 RewardServiceWithLoggingImpl.getReward() called");
        return reward;
    }

    // Get all available reward
    @Override
    public List<Rewards> getAllRewards() {
        List<Rewards> allRewards = rewardRepository.findAll();
        logger.info("🟢 RewardServiceWithLoggingImpl.getAllRewards() called");
        return allRewards;
    }

    // Update reward
    @Override
    public Rewards updateReward(String rewardId, Rewards reward) {
        Rewards rewardToUpdate = rewardRepository.findById(rewardId).get();
        rewardToUpdate.setId(reward.getId());
        rewardToUpdate.setName(reward.getName());
        rewardToUpdate.setDescription(reward.getDescription());
        rewardToUpdate.setCostInPoints(reward.getCostInPoints());
        rewardToUpdate.setAvailable(reward.isAvailable());
        rewardToUpdate.setCreatedAt(reward.getCreatedAt());
        rewardToUpdate.setUpdatedAt(reward.getUpdatedAt());
        logger.info("🟢 RewardServiceWithLoggingImpl.updateReward() called");
        return rewardRepository.save(rewardToUpdate);
    }

    // Delete reward
    @Override
    public void deleteReward(String rewardId) {
        rewardRepository.deleteById(rewardId);
        logger.info("🟢 RewardServiceWithLoggingImpl.deleteReward() called");
    }

     // Get all reward redemptions
    @Override
    public List<RewardRedemption> getAllRedemptions() {
        List<RewardRedemption> allRedemptions = rewardRedemptionRepository.findAll();
        logger.info("🟢 RewardServiceWithLoggingImpl.getAllRedemptions() called");
        return allRedemptions;
    }

    // Create reward redemptions
    @Override
    public RewardRedemption createRedemption(RewardRedemptionRequestDTO requestDTO) {
        // Fetch User and Reward
        Optional<User> userOpt = userRepository.findById(requestDTO.getUserId());
        Optional<Rewards> rewardOpt = rewardRepository.findById(requestDTO.getRewardId());

        if (userOpt.isEmpty() || rewardOpt.isEmpty()) {
            throw new RuntimeException("User or Reward not found!");
        }

        // Create new redemption
        RewardRedemption redemption = new RewardRedemption(userOpt.get(), rewardOpt.get(), requestDTO.getStatus());

        // Save to database
        return rewardRedemptionRepository.save(redemption);
    }

    // Get reward redemption by id
    @Override
    public RewardRedemption getRedemption(String rewardId) {
        RewardRedemption rewardRedemption = rewardRedemptionRepository.findById(rewardId).get();
        logger.info("🟢 RewardServiceWithLoggingImpl.getRedemption() called");
        return rewardRedemption;
    }

    // Update reward redemption
    @Override
    public RewardRedemption updateRedemption(String rewardId, RewardRedemption redemption) {
        RewardRedemption redemptionToUpdate = rewardRedemptionRepository.findById(rewardId).get();
        redemptionToUpdate.setId(redemption.getId());
        redemptionToUpdate.setStatus(redemption.getStatus());
        redemptionToUpdate.setCreatedAt(redemption.getCreatedAt());
        redemptionToUpdate.setUpdatedAt(redemption.getUpdatedAt());
        logger.info("🟢 RewardServiceWithLoggingImpl.updateRedemption() called");
        return rewardRedemptionRepository.save(redemptionToUpdate);
    }

    // Delete reward redemption
    // @Override
    // public void deleteRedemption(String rewardId) {
    //     rewardRedemptionRepository.deleteById(rewardId);
    //     logger.info("🟢 RewardServiceWithLoggingImpl.deleteRedemption() called");
    // }

    @Override
    public void deleteRedemption(String rewardId) {
        Optional<RewardRedemption> redemptions = rewardRedemptionRepository.findById(rewardId);

        if (!redemptions.isEmpty()) {
            throw new RuntimeException("Cannot delete reward: It is still referenced in redemptions.");
        }

        rewardRedemptionRepository.deleteById(rewardId);
    }

    /**
     * Redeems a reward for a user.
     * This method uses the Command pattern to encapsulate the redemption process.
     * 
     * @param userId The ID of the user redeeming the reward
     * @param rewardId The ID of the reward being redeemed
     * @return A RedemptionResult containing the result of the redemption
     */
    @Override
    @Transactional
    public RedemptionResult redeemReward(String userId, String rewardId) {
        logger.info("🟢 RewardServiceWithLoggingImpl.redeemReward() called for user {} and reward {}", userId, rewardId);
        
        // Use the Command pattern to execute the redemption
        RedeemRewardCommand command = RedeemRewardCommand.builder()
            .userId(userId)
            .rewardId(rewardId)
            .userRepository(userRepository)
            .rewardRepository(rewardRepository)
            .redemptionRepository(rewardRedemptionRepository)
            .pointsTransactionService(pointsTransactionService)
            .redemptionFactory(redemptionFactory)
            .messageBroker(messageBroker)
            .objectMapper(objectMapper)
            .build();
        
        RedemptionResult result = command.execute();
        
        // Notify observers if redemption was successful
        if (result.isSuccess() && result.getRedemptionId() != null) {
            RewardRedemption redemption = rewardRedemptionRepository.findById(result.getRedemptionId()).orElse(null);
            if (redemption != null && observers != null) {
                for (RedemptionObserver observer : observers) {
                    observer.onRedemptionCreated(redemption);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Completes a redemption.
     * This method transitions a redemption from PROCESSING to COMPLETED status.
     * 
     * @param redemptionId The ID of the redemption to complete
     * @return A RedemptionResult containing the result of the operation
     */
    @Transactional



    
    public RedemptionResult completeRedemption(String redemptionId) {
        logger.info("🟢 RewardServiceWithLoggingImpl.completeRedemption() called for redemption {}", redemptionId);
        
        RewardRedemption redemption = rewardRedemptionRepository.findById(redemptionId).orElse(null);
        if (redemption == null) {
            return RedemptionResult.builder()
                .success(false)
                .message("Redemption not found")
                .timestamp(ZonedDateTime.now())
                .build();
        }
        
        // Use the State pattern to handle the transition
        stateFactory.getStateForRedemption(redemption).complete(redemption);
        
        return RedemptionResult.builder()
            .success(true)
            .message("Redemption completed successfully")
            .redemptionId(redemptionId)
            .timestamp(ZonedDateTime.now())
            .build();
    }
    
    /**
     * Cancels a redemption.
     * This method transitions a redemption to CANCELLED status.
     * 
     * @param redemptionId The ID of the redemption to cancel
     * @return A RedemptionResult containing the result of the operation
     */
    @Transactional
    public RedemptionResult cancelRedemption(String redemptionId) {
        logger.info("🟢 RewardServiceWithLoggingImpl.cancelRedemption() called for redemption {}", redemptionId);
        
        RewardRedemption redemption = rewardRedemptionRepository.findById(redemptionId).orElse(null);
        if (redemption == null) {
            return RedemptionResult.builder()
                .success(false)
                .message("Redemption not found")
                .timestamp(ZonedDateTime.now())
                .build();
        }
        
        // Use the State pattern to handle the transition
        stateFactory.getStateForRedemption(redemption).cancel(redemption);
        
        return RedemptionResult.builder()
            .success(true)
            .message("Redemption cancelled successfully")
            .redemptionId(redemptionId)
            .timestamp(ZonedDateTime.now())
            .build();
    }

    // Count redemptions
    @Override
    public int countRedemptions() {
        return (int) rewardRedemptionRepository.count();
    }
}
