// Java class for RewardService
// RewardService.java

package sg.edu.ntu.gamify_demo.services;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import sg.edu.ntu.gamify_demo.interfaces.RewardService;
import sg.edu.ntu.gamify_demo.models.Reward;
import sg.edu.ntu.gamify_demo.repositories.RewardRepository;

@Service
@Component
public class RewardServiceWithLoggingImpl implements RewardService {

    private RewardRepository rewardRepository;
    private final Logger logger = LoggerFactory.getLogger(RewardService.class);

    // Constructor
    public RewardServiceWithLoggingImpl(RewardRepository rewardRepository) {
        this.rewardRepository = rewardRepository;
    }

    // Method
    // Save
    @Override
    public Reward createReward(Reward reward) {
        Reward newReward = rewardRepository.save(reward);
        logger.info("🟢 RewardServiceWithLoggingImpl.createReward() called");
        return newReward;
    }

    // Get One
    @Override
    public Reward getReward(Long id) {
        Reward reward = rewardRepository.findById(id).get();
        logger.info("🟢 RewardServiceWithLoggingImpl.getReward() called");
        return reward;
    }

    // Get All
    @Override
    public List<Reward> getAllRewards() {
        List<Reward> allRewards = rewardRepository.findAll();
        logger.info("🟢 RewardServiceWithLoggingImpl.getAllRewards() called");
        return allRewards;
    }

    // Update
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

    // Delete
    @Override
    public void deleteReward(Long id) {
        rewardRepository.deleteById(id);
        logger.info("🟢 RewardServiceWithLoggingImpl.deleteReward() called");
    }
}
