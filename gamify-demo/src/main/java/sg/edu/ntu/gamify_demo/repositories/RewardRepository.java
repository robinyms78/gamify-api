// Java class for RewardRepository
// RewardRepository.java

package sg.edu.ntu.gamify_demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sg.edu.ntu.gamify_demo.models.Rewards;

public interface RewardRepository extends JpaRepository<Rewards, String> {
}
