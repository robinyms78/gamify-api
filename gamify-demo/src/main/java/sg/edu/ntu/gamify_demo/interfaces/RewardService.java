package sg.edu.ntu.gamify_demo.interfaces;

import java.util.List;
import sg.edu.ntu.gamify_demo.models.Rewards;

public interface RewardService {
    List<Rewards> getAllRewards();

    Rewards saveReward(Rewards reward);
    
    Rewards getReward(String rewardId);
    
    Rewards updateReward(String rewardId, Rewards reward);
    
    void deleteReward(String rewardId);
}
