// Java class for RewardControllerWithLoggingImpl
// RewardControllerWithLoggingImpl.java

package sg.edu.ntu.gamify_demo.controllers;

import java.util.List;
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
import sg.edu.ntu.gamify_demo.dtos.RedemptionResult;
import sg.edu.ntu.gamify_demo.dtos.RewardRedemptionRequest;
import sg.edu.ntu.gamify_demo.interfaces.RewardRedemptionService;
import sg.edu.ntu.gamify_demo.interfaces.RewardService;
import sg.edu.ntu.gamify_demo.models.Reward;
import sg.edu.ntu.gamify_demo.models.RewardRedemption;

@RestController
@RequestMapping("/rewards")
public class RewardControllerWithLoggingImpl {
    
    private final RewardService rewardService;
    private final RewardRedemptionService rewardRedemptionService;

    // Constructor injection
    public RewardControllerWithLoggingImpl(RewardService rewardService, RewardRedemptionService rewardRedemptionService) {
        this.rewardService = rewardService;
        this.rewardRedemptionService = rewardRedemptionService;
    }

    // Save reward
    @PostMapping("")
    public ResponseEntity<Reward> createReward(@RequestBody Reward reward) {
        Reward newReward = rewardService.saveReward(reward);
        return new ResponseEntity<>(newReward, HttpStatus.CREATED);
    } 

    // Get all reward
    @GetMapping("")
    public ResponseEntity<List<Reward>> getAllRewards() {
        List<Reward> allRewards = rewardService.getAllRewards();
        return new ResponseEntity<>(allRewards, HttpStatus.OK);
    }

    // Get one reward by rewardId
    @GetMapping("/{id}")
    public ResponseEntity<Reward> getReward(@PathVariable String rewardId) {
        Reward foundReward = rewardService.getReward(rewardId);
        return new ResponseEntity<>(foundReward, HttpStatus.OK);
    }

    // Update reward
    @PutMapping("/{id}")
    public ResponseEntity<Reward> updateReward(@PathVariable String rewardId, @RequestBody Reward reward) {
        Reward updateReward = rewardService.updateReward(rewardId, reward);
        return new ResponseEntity<>(updateReward, HttpStatus.OK);
    }

    // Delete reward
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteReward(@PathVariable String rewardId) {
        rewardService.deleteReward(rewardId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Save reward redemption
    @PostMapping("/redemption")
    public ResponseEntity<RewardRedemption> saveRedemption(@RequestBody RewardRedemption redemption) {
        RewardRedemption newRedemption = rewardRedemptionService.saveRedemption(redemption);
        return new ResponseEntity<>(newRedemption, HttpStatus.CREATED);
    } 

    // Get all available reward redemption
    @GetMapping("/redemption")
    public ResponseEntity<List<RewardRedemption>> getAllRedemptions() {
        List<RewardRedemption> allRedemptions = rewardRedemptionService.getAllRedemptions();
        return new ResponseEntity<>(allRedemptions, HttpStatus.OK);
    }

    // Get one reward redemption by redemption Id
    @GetMapping("/redemption/{id}")
    public ResponseEntity<RewardRedemption> getRedemption(@PathVariable String rewardId) {
        RewardRedemption foundRedemption = rewardRedemptionService.getRedemption(rewardId);
        return new ResponseEntity<>(foundRedemption, HttpStatus.OK);
    }

    // Update reward redemption
    @PutMapping("/redemption/{id}")
    public ResponseEntity<RewardRedemption> updateRedemption(@PathVariable String rewardId, @RequestBody RewardRedemption redemption) {
        RewardRedemption updateRedemption = rewardRedemptionService.updateRedemption(rewardId, redemption);
        return new ResponseEntity<>(updateRedemption, HttpStatus.OK);
    }

    // Delete reward redemption
    @DeleteMapping("/redemption/{id}")
    public ResponseEntity<HttpStatus> deleteRedemption(@PathVariable String rewardId) {
        rewardRedemptionService.deleteRedemption(rewardId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Redeems a reward for a user.
     * 
     * @param request The redemption request containing user and reward IDs
     * @return The result of the redemption operation
     */
    @PostMapping("/redeem")
    public ResponseEntity<?> redeemReward(@RequestBody RewardRedemptionRequest request) {
        RedemptionResult result = rewardRedemptionService.redeemReward(request.getUserId(), request.getRewardId());
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
    }
    
    /**
     * Completes a redemption.
     * 
     * @param redemptionId The ID of the redemption to complete
     * @return The result of the operation
     */
    @PostMapping("/redemption/{id}/complete")
    public ResponseEntity<?> completeRedemption(@PathVariable("id") String redemptionId) {
        RedemptionResult result = rewardRedemptionService.completeRedemption(redemptionId);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
    }
    
    /**
     * Cancels a redemption.
     * 
     * @param redemptionId The ID of the redemption to cancel
     * @return The result of the operation
     */
    @PostMapping("/redemption/{id}/cancel")
    public ResponseEntity<?> cancelRedemption(@PathVariable("id") String redemptionId) {
        RedemptionResult result = rewardRedemptionService.cancelRedemption(redemptionId);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
    }

    // Count redemptions
    @GetMapping("/redemption/count")
    public ResponseEntity<Integer> countRedemptions() {
        Integer count = rewardRedemptionService.countRedemptions();
        return new ResponseEntity<>(count, HttpStatus.OK);
    }
}
