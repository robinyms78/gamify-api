package sg.edu.ntu.gamify_demo.interfaces;

import java.util.List;
import sg.edu.ntu.gamify_demo.models.Reward;

public interface RewardService {
    List<Reward> getAllRewards();
    Reward createReward(Reward reward);
    Reward getReward(Long id);
    Reward updateReward(Long id, Reward reward);
    void deleteReward(Long id);
}
