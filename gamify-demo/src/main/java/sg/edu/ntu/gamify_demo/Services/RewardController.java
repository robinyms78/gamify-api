package sg.edu.ntu.gamify_demo.Services;

import java.util.ArrayList;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rewards")
public class RewardController {
    
    private ArrayList<Reward> rewards = new ArrayList<>();

    // Constructor
    public RewardController() {
        rewards.add(new Reward("laptop", "electronics", 1000, true));
        rewards.add(new Reward("voucher", "grocery", 100, true));
        rewards.add(new Reward("iPhone", "electronics", 500, true));
        rewards.add(new Reward("airticket", "travel", 2000, false));
    }

    // Post reward
    @PostMapping("")
    public ResponseEntity<Reward> createReward(@RequestBody Reward reward) {
        rewards.add(reward);
        return new ResponseEntity<>(reward, HttpStatus.CREATED);
    } 

    // Get all reward
    @GetMapping("")
    public ResponseEntity<ArrayList<Reward>> getAllRewards() {
        return new ResponseEntity<>(rewards, HttpStatus.OK);
    }

    // Get one reward by rewardId
    @GetMapping("/{id}")
    public ResponseEntity<Reward> getReward(@PathVariable String id) {
        int index = getRewardIndex(id);
        return new ResponseEntity<>(rewards.get(index), HttpStatus.OK);
    }

    // Helper method
    private int getRewardIndex(String id) {
        for( Reward reward: rewards) {
            if(reward.getRewardId().equals(id)) {
                return rewards.indexOf(reward);
            }
        }

        // Not found
        throw new RewardNotFoundException(id); 
    }

    // Update reward
    @PutMapping("/{id}")
    public ResponseEntity<Reward> updateReward(@PathVariable String id, @RequestBody Reward reward) {
        int index = getRewardIndex(id);

        // Retrieve the reward from the list
        Reward updateReward = rewards.get(index);

        if ( index == -1) {
            rewards.set(index, reward);
            return new ResponseEntity<>(reward, HttpStatus.CREATED);
        }

        updateReward.setName(reward.getName());
        updateReward.setDescription(reward.getDescription());
        updateReward.setCostInPoints(reward.getCostInPoints());
        updateReward.setAvailable(reward.isAvailable());

        return new ResponseEntity<>(updateReward, HttpStatus.CREATED);
    }

    // Delete reward
    @DeleteMapping("/{id}")
    public ResponseEntity<Reward> deleteReward(@PathVariable String id) {
        int index = getRewardIndex(id);
        return new ResponseEntity<>(rewards.remove(index), HttpStatus.NOT_FOUND);
    }
}