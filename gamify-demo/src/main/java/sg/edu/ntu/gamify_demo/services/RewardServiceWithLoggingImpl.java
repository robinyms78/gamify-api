// Java class for RewardService
// RewardService.java

package sg.edu.ntu.gamify_demo.services;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import sg.edu.ntu.gamify_demo.dtos.RedemptionResult;
import sg.edu.ntu.gamify_demo.interfaces.RewardRedemptionService;
import sg.edu.ntu.gamify_demo.interfaces.RewardService;
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.models.Reward;
import sg.edu.ntu.gamify_demo.models.RewardRedemption;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.repositories.RewardRedemptionRepository;
import sg.edu.ntu.gamify_demo.repositories.RewardRepository;

@Service
@Component
public class RewardServiceWithLoggingImpl implements RewardService, RewardRedemptionService {

    private final UserService userService;
    private final RewardRepository rewardRepository;
    private final RewardRedemptionRepository rewardRedemptionRepository;
    private final Logger logger = LoggerFactory.getLogger(RewardService.class);

    // Constructor
    public RewardServiceWithLoggingImpl(RewardRepository rewardRepository, RewardRedemptionRepository rewardRedemptionRepository, UserService userService) {
        this.rewardRepository = rewardRepository;
        this.rewardRedemptionRepository = rewardRedemptionRepository;
        this.userService = userService;
    }

    // Method
    // Save reward
    @Override
    public Reward saveReward(Reward reward) {
        Reward newReward = rewardRepository.save(reward);
        logger.info("🟢 RewardServiceWithLoggingImpl.createReward() called");
        return newReward;
    }

    // Get reward by id
    @Override
    public Reward getReward(String rewardId) {
        Reward reward = rewardRepository.findById(rewardId).get();
        logger.info("🟢 RewardServiceWithLoggingImpl.getReward() called");
        return reward;
    }

    // Get all available reward
    @Override
    public List<Reward> getAllRewards() {
        List<Reward> allRewards = rewardRepository.findAll();
        logger.info("🟢 RewardServiceWithLoggingImpl.getAllRewards() called");
        return allRewards;
    }

    // Update reward
    @Override
    public Reward updateReward(String rewardId, Reward reward) {
        Reward rewardToUpdate = rewardRepository.findById(rewardId).get();
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

    // Save reward redemptions
    @Override
    public RewardRedemption saveRedemption(RewardRedemption redemption) {
        RewardRedemption newRedemption = rewardRedemptionRepository.save(redemption);
        logger.info("🟢 RewardServiceWithLoggingImpl.saveRedemption() called");
        return newRedemption;
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
    @Override
    public void deleteRedemption(String rewardId) {
        rewardRedemptionRepository.deleteById(rewardId);
        logger.info("🟢 RewardServiceWithLoggingImpl.deleteRedemption() called");
    }

    // Redeem reward
    @Override
    public RedemptionResult redeemReward(String userId, String rewardId) {
        User user = userService.getUserById(userId);
        Reward reward = rewardRepository.findById(rewardId).get();
        Long userPoints = user.getEarnedPoints();
        Long rewardPoints = reward.getCostInPoints();
        if(userPoints < rewardPoints) {
            return new RedemptionResult(false, "Insufficient points");
        } else {
            userService.deductPoints(userId, rewardPoints.intValue());
            createRedemptionRecord(user, reward);
            return new RedemptionResult(true, "Reward redeemed successfully");
        }
    }

    // Helper method to create redemption records
    private void createRedemptionRecord(User user, Reward reward) {
        RewardRedemption redemption = new RewardRedemption(user, reward, "PROCESSING");
        rewardRedemptionRepository.save(redemption);
    }

    // Count redemptions
    @Override
    public int countRedemptions() {
        return (int) rewardRedemptionRepository.count();
    }
}
