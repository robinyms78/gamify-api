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
import sg.edu.ntu.gamify_demo.interfaces.RewardService;
import sg.edu.ntu.gamify_demo.models.Reward;
import sg.edu.ntu.gamify_demo.services.RewardServiceWithLoggingImpl;

@RestController
@RequestMapping("/rewards")
@Tag(name = "Rewards", description = "Endpoints for managing rewards system")
public class RewardControllerWithLoggingImpl {
    
    private RewardService rewardService;

    // Constructor injection
    public RewardControllerWithLoggingImpl(RewardServiceWithLoggingImpl rewardService) {
        this.rewardService = rewardService;
    }

    // Save reward
    @PostMapping("")
    public ResponseEntity<Reward> createReward(@RequestBody Reward reward) {
        Reward newReward = rewardService.createReward(reward);
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
}
