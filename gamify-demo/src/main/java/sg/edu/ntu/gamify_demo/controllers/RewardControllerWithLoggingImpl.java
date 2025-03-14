// Java class for RewardControllerWithLoggingImpl
// RewardControllerWithLoggingImpl.java

package sg.edu.ntu.gamify_demo.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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

import sg.edu.ntu.gamify_demo.interfaces.RewardRedemptionService;
import sg.edu.ntu.gamify_demo.interfaces.RewardService;
import sg.edu.ntu.gamify_demo.models.Reward;
import sg.edu.ntu.gamify_demo.models.RewardRedemption;
import sg.edu.ntu.gamify_demo.services.RewardServiceWithLoggingImpl;

@RestController
@RequestMapping("/rewards")
@Tag(name = "Rewards", description = "Endpoints for managing rewards system")
public class RewardControllerWithLoggingImpl {
    
    private RewardService rewardService;
    private RewardRedemptionService rewardRedemptionService;

    // Constructor injection
    public RewardControllerWithLoggingImpl(RewardServiceWithLoggingImpl rewardService, RewardServiceWithLoggingImpl rewardRedemptionService) {
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
    public ResponseEntity<Reward> getReward(@PathVariable Long id) {
        Reward foundReward = rewardService.getReward(id);
        return new ResponseEntity<>(foundReward, HttpStatus.OK);
    }

    // Update reward
    @PutMapping("/{id}")
    public ResponseEntity<Reward> updateReward(@PathVariable Long id, @RequestBody Reward reward) {
        Reward updateReward = rewardService.updateReward(id, reward);
        return new ResponseEntity<>(updateReward, HttpStatus.OK);
    }

    // Delete reward
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteReward(@PathVariable Long id) {
        rewardService.deleteReward(id);
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
    public ResponseEntity<RewardRedemption> getRedemption(@PathVariable Long id) {
        RewardRedemption foundRedemption = rewardRedemptionService.getRedemption(id);
        return new ResponseEntity<>(foundRedemption, HttpStatus.OK);
    }

    // Update reward redemption
    @PutMapping("/redemption/{id}")
    public ResponseEntity<RewardRedemption> updateRedemption(@PathVariable Long id, @RequestBody RewardRedemption redemption) {
        RewardRedemption updateRedemption = rewardRedemptionService.updateRedemption(id, redemption);
        return new ResponseEntity<>(updateRedemption, HttpStatus.OK);
    }

    // Delete reward redemption
    @DeleteMapping("/redemption/{id}")
    public ResponseEntity<HttpStatus> deleteRedemption(@PathVariable Long id) {
        rewardRedemptionService.deleteRedemption(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // // Redeem reward
    // @PostMapping("/redemption")
    // public ResponseEntity<String> redeemReward(Long userId, Long id) {
    //     rewardRedemptionService.redeemReward(userId, id);
    //     return new ResponseEntity<>(HttpStatus.OK);
    // }

    // // Count redemptions
    // @GetMapping("/redemption/count")
    // public ResponseEntity<Integer> countRedemptions() {
    //     Integer count = rewardRedemptionService.countRedemptions();
    //     return new ResponseEntity<>(count, HttpStatus.OK);
    // }
}
