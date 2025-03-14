// Java class for RewardRedemptionRepository
// RewardRedemptionRepository.java

package sg.edu.ntu.gamify_demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sg.edu.ntu.gamify_demo.models.RewardRedemption;

public interface RewardRedemptionRepository extends JpaRepository<RewardRedemption, String> {
    
}