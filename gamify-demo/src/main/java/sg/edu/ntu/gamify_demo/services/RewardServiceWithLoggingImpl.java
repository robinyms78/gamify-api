// Java class for RewardService
// RewardService.java

package sg.edu.ntu.gamify_demo.Services;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import sg.edu.ntu.gamify_demo.exceptions.RewardNotFoundException;
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
    // Create
    @Override
    public Reward createReward(Reward reward) {
        logger.info("🟢 RewardServiceImpl.createReward() called");
        return rewardRepository.createReward(reward);
    }

    // Get One
    public Reward getReward(String id) {
        logger.info("🟢 RewardServiceImpl.getReward() called");
        return rewardRepository.getReward(getRewardIndex(id));
    }

    // Get All
    public ArrayList<Reward> getAllRewards() {
        logger.info("🟢 RewardServiceImpl.getAllReward() called");
        return rewardRepository.getAllRewards();
    }

    // Update
    public Reward updateReward(String id, Reward reward) {
        logger.info("🟢 RewardServiceImpl.updateReward() called");
        return rewardRepository.updatReward(getRewardIndex(id), reward);
    }

    // Delete
    public void deleteReward(String id) {
        logger.info("🟢 RewardServiceImpl.deleteReward() called");
        rewardRepository.deleteReward(getRewardIndex(id));
    }

    // Helper method
    private int getRewardIndex(String id) {

        for (Reward reward : rewardRepository.getAllRewards()) {
            if(reward.getRewardId().equals(id)) {
                return rewardRepository.getAllRewards().indexOf(reward);
            }
        }

        throw new RewardNotFoundException(id);
    }
}
