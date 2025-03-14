package sg.edu.ntu.gamify_demo.interfaces;

import java.util.List;
import sg.edu.ntu.gamify_demo.models.Reward;

public interface RewardService {
    List<Reward> getAllRewards();

    Reward saveReward(Reward reward);
    
    Reward getReward(String rewardId);
    
    Reward updateReward(String rewardId, Reward reward);
    
    void deleteReward(String rewardId);
}
