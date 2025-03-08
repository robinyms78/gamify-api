// Java class for Redemption
// Redemption.java

package sg.edu.ntu.gamify_demo.models;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The Redemption class represents a class which check the reward redemption status the user in the system.
 * It stores the redemption details including redemptionId and timestamps for creation and updates.
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Redemption {
    // Create instance of User class
    User user = new User();
    // Create instance of Reward class
    Reward reward = new Reward();
    
    //Instance variables
    private String redemptionId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Constructs a Redemption object with the provided details.
     * 
     * @param user The instance of the User.
     * @param reward The instance of the Reward.
     * @param userId The unique identifier for the user.
     * @param rewardId The unique identifier for the reward.
     * @param costInpoints The points associated with the reward.
     * @param name The name of the reward.
     * @param description The description of the reward.
     * @param available The availability of the reward.
     * @param createdAt The timestamp of when the reward was created.
     * @param updatedAt The timestamp of when the reward was last updated.
     */

    public Redemption(String userId, String rewardId, String redemptionId, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        user.userId = userId;
        reward.rewardId = rewardId;
        this.redemptionId = UUID.randomUUID().toString();
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}