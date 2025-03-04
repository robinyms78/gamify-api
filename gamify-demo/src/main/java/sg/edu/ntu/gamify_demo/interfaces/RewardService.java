package sg.edu.ntu.gamify_demo.interfaces;

import java.util.ArrayList;
import sg.edu.ntu.gamify_demo.models.Reward;

public interface RewardService {
    Reward createReward(Reward reward);
    Reward getReward(String id);
    ArrayList<Reward> getAllRewards();
    Reward updateReward(String id, Reward reward);
    void deleteReward(String id);
}