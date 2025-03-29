// Java class for RewardRedemptionRepository
// RewardRedemptionRepository.java

package sg.edu.ntu.gamify_demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import sg.edu.ntu.gamify_demo.models.RewardRedemption;

public interface RewardRedemptionRepository extends JpaRepository<RewardRedemption, String> {
    @Transactional
    @Modifying
    @Query("UPDATE RewardRedemption r SET r.reward = NULL WHERE r.reward.id = :rewardId")
    void updateRewardToNull(@Param("rewardId") String rewardId);
}