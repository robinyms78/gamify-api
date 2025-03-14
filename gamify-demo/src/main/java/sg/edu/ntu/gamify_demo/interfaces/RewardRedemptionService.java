package sg.edu.ntu.gamify_demo.interfaces;

import java.util.List;
import sg.edu.ntu.gamify_demo.models.RewardRedemption;

public interface RewardRedemptionService {
    List<RewardRedemption> getAllRedemptions();

    RewardRedemption saveRedemption(RewardRedemption redemption);

    RewardRedemption getRedemption(Long id);

    RewardRedemption updateRedemption(Long id, RewardRedemption redemption);
    
    void deleteRedemption(Long id);

    String redeemReward(Long userId,Long id);

    Integer countRedemptions();
}