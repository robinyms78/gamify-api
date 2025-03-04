// Java class for RewardRepository
// RewardRepository.java

package sg.edu.ntu.gamify_demo.repositories;

import java.util.ArrayList;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import sg.edu.ntu.gamify_demo.models.Reward;

@Repository
@Component
public class RewardRepository {
    
    private ArrayList<Reward> rewards = new ArrayList<>();

    // Preload data here now
    public RewardRepository() {
        rewards.add(new Reward("organizer", "stationery", 300, true));
        rewards.add(new Reward("NTUC voucher", "grocery", 500, true));
        rewards.add(new Reward("iphone", "electronics", 1000, true));
        rewards.add(new Reward("laptop", "electronics", 3000, false));
    }

    // Create
    public Reward createReward(Reward reward) {
        rewards.add(reward);
        return reward;
    }

    // Get One
    public Reward getReward(int index) {
        return rewards.get(index);
    }

    // Get All
    public ArrayList<Reward> getAllRewards() {
        return rewards;
    }

    // Update
    public Reward updatReward(int index, Reward reward) {
        Reward rewardToUpdate = rewards.get(index);
        rewardToUpdate.setName(reward.getName());
        rewardToUpdate.setDescription(reward.getDescription());
        rewardToUpdate.setCostInPoints(reward.getCostInPoints());
        rewardToUpdate.setAvailable(reward.isAvailable());
        return rewardToUpdate;
    }

    // Delete
    public void deleteReward(int index) {
        rewards.remove(index);
    }
}
