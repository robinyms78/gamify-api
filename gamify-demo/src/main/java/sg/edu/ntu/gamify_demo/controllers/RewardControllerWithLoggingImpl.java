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
import sg.edu.ntu.gamify_demo.dtos.RewardRedemptionRequestDTO;
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
    public ResponseEntity<RewardRedemption> createRedemption(@RequestBody RewardRedemptionRequestDTO requestDTO) {
        RewardRedemption redemption = rewardRedemptionService.createRedemption(requestDTO);
        return new ResponseEntity<>(redemption, HttpStatus.CREATED);
    } 

    // Get all available reward redemption
    @GetMapping("/redemption")
    public ResponseEntity<List<RewardRedemption>> getAllRedemptions() {
        List<RewardRedemption> allRedemptions = rewardRedemptionService.getAllRedemptions();
        return new ResponseEntity<>(allRedemptions, HttpStatus.OK);
    }

    // Get one reward redemption by redemption Id
    @GetMapping("/redemption/{redemptionId}")
    @Operation(summary = "Get reward redemption by redemption Id", 
               description = "Retrieves reward redemption by redemption Id")
    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved reward redemption",
                 content = @Content(schema = @Schema(implementation = RedemptionResult.class))),
    @ApiResponse(responseCode = "404", description = "Reward redemption not found")
                })
    public ResponseEntity<RewardRedemption> getRedemption(@PathVariable String redemptionId) {
        RewardRedemption foundRedemption = rewardRedemptionService.getRedemption(redemptionId);
        return new ResponseEntity<>(foundRedemption, HttpStatus.OK);
    }

    // Update reward redemption
    @PutMapping("/redemption/{redemptionId}")
    @Operation(summary = "Update reward redemption by redemption Id", 
    description = "Update reward redemption by redemption Id")
    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully updated reward redemption",
                content = @Content(schema = @Schema(implementation = RedemptionResult.class))),
    @ApiResponse(responseCode = "404", description = "Reward redemption not found")
                })
    public ResponseEntity<RewardRedemption> updateRedemption(@PathVariable String redemptionId, @RequestBody RewardRedemption redemption) {
        RewardRedemption updateRedemption = rewardRedemptionService.updateRedemption(redemptionId, redemption);
        return new ResponseEntity<>(updateRedemption, HttpStatus.OK);
    }

    // Delete reward redemption
    @DeleteMapping("/redemption/{redemptionId}")
    public ResponseEntity<HttpStatus> deleteRedemption(@PathVariable String redemptionId) {
        rewardRedemptionService.deleteRedemption(redemptionId);
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
                  content = @Content(schema = @Schema(implementation = RewardRedemptionRequestDTO.class)))
        @RequestBody RewardRedemptionRequestDTO request) {
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
    @PostMapping("/redemption/complete/{redemptionId}")
    @Operation(summary = "Redeem reward completed", description = "Redeems a reward for a user completed")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Redemption complete successful",
                    content = @Content(schema = @Schema(implementation = RedemptionResult.class))),
        @ApiResponse(responseCode = "400", description = "Redemption complete failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @PostMapping("/redemption/cancel/{redemptionId}")
    @Operation(summary = "Redeem reward cancelled", description = "Redeems a reward for a user cancelled")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Redemption cancelled successful",
                    content = @Content(schema = @Schema(implementation = RedemptionResult.class))),
        @ApiResponse(responseCode = "400", description = "Redemption cancelled failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
