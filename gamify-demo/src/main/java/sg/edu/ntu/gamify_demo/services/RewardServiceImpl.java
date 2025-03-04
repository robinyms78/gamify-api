// Java class for RewardService
// RewardService.java

package sg.edu.ntu.gamify_demo.services;

import java.util.ArrayList;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import sg.edu.ntu.gamify_demo.exceptions.RewardNotFoundException;
import sg.edu.ntu.gamify_demo.interfaces.RewardService;
import sg.edu.ntu.gamify_demo.models.Reward;
import sg.edu.ntu.gamify_demo.repositories.RewardRepository;

@Service
@Component
public class RewardServiceImpl implements RewardService {

    private RewardRepository rewardRepository;

    // Constructor
    public RewardServiceImpl(RewardRepository rewardRepository) {
        this.rewardRepository = rewardRepository;
    }

    // Method
    // Create
    public Reward createReward(Reward reward) {
        return rewardRepository.createReward(reward);
    }

    // Get One
    public Reward getReward(String id) {
        return rewardRepository.getReward(getRewardIndex(id));
    }

    // Get All
    public ArrayList<Reward> getAllRewards() {
        return rewardRepository.getAllRewards();
    }

    // Update
    public Reward updateReward(String id, Reward reward) {
        return rewardRepository.updatReward(getRewardIndex(id), reward);
    }

    // Delete
    public void deleteReward(String id) {
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
