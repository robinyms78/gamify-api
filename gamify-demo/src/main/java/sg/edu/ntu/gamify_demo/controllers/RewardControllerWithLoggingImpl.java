// Java class for RewardControllerWithLoggingImpl
// RewardControllerWithLoggingImpl.java

package sg.edu.ntu.gamify_demo.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
import sg.edu.ntu.gamify_demo.dtos.RewardsDTO;
import sg.edu.ntu.gamify_demo.dtos.ErrorResponse;
import sg.edu.ntu.gamify_demo.interfaces.RewardRedemptionService;
import sg.edu.ntu.gamify_demo.interfaces.RewardService;
import sg.edu.ntu.gamify_demo.models.RewardRedemption;
import sg.edu.ntu.gamify_demo.models.Rewards;

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
    public ResponseEntity<Rewards> createReward(@RequestBody Rewards reward) {
        Rewards newReward = rewardService.saveReward(reward);
        return new ResponseEntity<>(newReward, HttpStatus.CREATED);
    } 

    // Get all reward
    @GetMapping("")
    public ResponseEntity<List<Rewards>> getAllRewards() {
        List<Rewards> allRewards = rewardService.getAllRewards();
        return new ResponseEntity<>(allRewards, HttpStatus.OK);
    }

    // Get one reward by rewardId
    @GetMapping("/{rewardId}")
    @Operation(summary = "Get reward by reward Id", 
              description = "Retrieves reward by reward Id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved reward",
                    content = @Content(schema = @Schema(implementation = RewardsDTO.class))),
        @ApiResponse(responseCode = "404", description = "Reward not found")
    })
    public ResponseEntity<Rewards> getReward(@PathVariable String rewardId) {
        Rewards foundReward = rewardService.getReward(rewardId);
        return new ResponseEntity<>(foundReward, HttpStatus.OK);
    }

    // Update reward
    @PutMapping("/{rewardId}")
    @Operation(summary = "Update reward", 
              description = "Update reward by reward Id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status updated successfully",
                    content = @Content(schema = @Schema(implementation = RewardsDTO.class))),
        @ApiResponse(responseCode = "404", description = "Reward not found")
    })
    public ResponseEntity<Rewards> updateReward(@PathVariable String rewardId, @RequestBody Rewards reward) {
        Rewards updateReward = rewardService.updateReward(rewardId, reward);
        return new ResponseEntity<>(updateReward, HttpStatus.OK);
    }

    // Delete reward
    @DeleteMapping("/{rewardId}")
    @Operation(summary = "Delete reward", 
               description = "Remove a reward by Id")
    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Reward deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Reward not found"),
                })
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
    @GetMapping("/redemption/{rewardId}")
    public ResponseEntity<RewardRedemption> getRedemption(@PathVariable String rewardId) {
        RewardRedemption foundRedemption = rewardRedemptionService.getRedemption(rewardId);
        return new ResponseEntity<>(foundRedemption, HttpStatus.OK);
    }

    // Update reward redemption
    @PutMapping("/redemption/{rewardId}")
    public ResponseEntity<RewardRedemption> updateRedemption(@PathVariable String rewardId, @RequestBody RewardRedemption redemption) {
        RewardRedemption updateRedemption = rewardRedemptionService.updateRedemption(rewardId, redemption);
        return new ResponseEntity<>(updateRedemption, HttpStatus.OK);
    }

    // Delete reward redemption
    @DeleteMapping("/redemption/{rewardId}")
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
    @Operation(summary = "Redeem reward", description = "Redeems a reward for a user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Redemption successful",
                    content = @Content(schema = @Schema(implementation = RedemptionResult.class))),
        @ApiResponse(responseCode = "400", description = "Redemption failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> redeemReward(
        @Parameter(description = "Redemption request details", required = true,
                  content = @Content(schema = @Schema(implementation = RewardRedemptionRequest.class)))
        @RequestBody RewardRedemptionRequest request) {
        try {
            RedemptionResult result = rewardRedemptionService.redeemReward(request.getUserId(), request.getRewardId());
            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse("Server error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Completes a redemption.
     * 
     * @param redemptionId The ID of the redemption to complete
     * @return The result of the operation
     */
    @PostMapping("/redemption/{redemptionId}/complete")
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
    @PostMapping("/redemption/{redemptionId}/cancel")
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
