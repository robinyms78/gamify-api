package sg.edu.ntu.gamify_demo.interfaces;

import java.util.List;

import sg.edu.ntu.gamify_demo.dtos.RedemptionResult;
import sg.edu.ntu.gamify_demo.models.RewardRedemption;

public interface RewardRedemptionService {
    List<RewardRedemption> getAllRedemptions();

    RewardRedemption saveRedemption(RewardRedemption redemption);

    RewardRedemption getRedemption(String rewardId);

    RewardRedemption updateRedemption(String rewardId, RewardRedemption redemption);
    
    void deleteRedemption(String rewardId);

    RedemptionResult redeemReward(String userId, String rewardId);

    int countRedemptions();
}