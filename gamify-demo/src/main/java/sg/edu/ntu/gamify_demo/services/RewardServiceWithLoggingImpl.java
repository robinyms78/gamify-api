// Java class for RewardService
// RewardService.java

package sg.edu.ntu.gamify_demo.Services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import sg.edu.ntu.gamify_demo.interfaces.RewardRedemptionService;
import sg.edu.ntu.gamify_demo.interfaces.RewardService;
import sg.edu.ntu.gamify_demo.models.Reward;
import sg.edu.ntu.gamify_demo.models.RewardRedemption;
import sg.edu.ntu.gamify_demo.repositories.RewardRedemptionRepository;
import sg.edu.ntu.gamify_demo.repositories.RewardRepository;

@Service
@Component
public class RewardServiceWithLoggingImpl implements RewardService, RewardRedemptionService {

    private final RewardRepository rewardRepository;
    private final Logger logger = LoggerFactory.getLogger(RewardService.class);

    // Constructor
    public RewardServiceWithLoggingImpl(RewardRepository rewardRepository, RewardRedemptionRepository rewardRedemptionRepository) {
        this.rewardRepository = rewardRepository;
        this.rewardRedemptionRepository = rewardRedemptionRepository;
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
    public Reward getReward(Long id) {
        Reward reward = rewardRepository.findById(id).get();
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
    public Reward updateReward(Long id, Reward reward) {
        Reward rewardToUpdate = rewardRepository.findById(id).get();
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
    public void deleteReward(Long id) {
        rewardRepository.deleteById(id);
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
    public RewardRedemption getRedemption(Long id) {
        RewardRedemption rewardRedemption = rewardRedemptionRepository.findById(id).get();
        logger.info("🟢 RewardServiceWithLoggingImpl.getRedemption() called");
        return rewardRedemption;
    }

    // Update reward redemption
    @Override
    public RewardRedemption updateRedemption(Long id, RewardRedemption redemption) {
        RewardRedemption redemptionToUpdate = rewardRedemptionRepository.findById(id).get();
        redemptionToUpdate.setId(redemption.getId());
        redemptionToUpdate.setStatus(redemption.getStatus());
        redemptionToUpdate.setCreatedAt(redemption.getCreatedAt());
        redemptionToUpdate.setUpdatedAt(redemption.getUpdatedAt());
        logger.info("🟢 RewardServiceWithLoggingImpl.updateRedemption() called");
        return rewardRedemptionRepository.save(redemptionToUpdate);
    }

    // Delete reward redemption
    @Override
    public void deleteRedemption(Long id) {
        rewardRedemptionRepository.deleteById(id);
        logger.info("🟢 RewardServiceWithLoggingImpl.deleteRedemption() called");
    }

    // Redeem reward
    @Override
    public String redeemReward(Long userId, Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'redeemReward'");
    }

    // Count redemptions
    @Override
    public Integer countRedemptions() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'countRedemptions'");
    }
}
