package sg.edu.ntu.gamify_demo.models;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The Redemption class represents a class which check the reward redemption status the user in the system.
 * It stores the redemption details including redemptionId and timestamps for creation and updates.
 */

public class Redemption {
    // Create instance of User class
    User user = new User();
    // Create instance of Reward class
    Reward reward = new Reward();
    
    //Instance variables
    private String redemptionId, status;
    private LocalDateTime createdAt, updatedAt;

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

    /**
     * Gets the instance of the user.
     * 
     * @return The instance of the user.
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the instance of the user.
     * 
     * @param user The instance of the new user.
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Gets the instance the reward.
     * 
     * @return The instance of the reward.
     */
    public Reward getReward() {
        return reward;
    }

    /**
     * Sets the instance of the reward.
     * 
     * @param user The instance of the new reward.
     */
    public void setReward(Reward reward) {
        this.reward = reward;
    }

    /**
     * Gets the redemption Id.
     * 
     * @return The redemption Id.
     */
    public String getRedemptionId() {
        return redemptionId;
    }

    /**
     * Gets the status of the redemption.
     * 
     * @return The status of the redemption.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the redemption.
     * 
     * @param user The status of the redemption.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    
    /**
     * Gets the creation timestamp of the redemption.
     * 
     * @return The timestamp when the redemption was created.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp of the redemption.
     * 
     * @param createdAt The new timestamp for when the redemption was created.
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the last updated timestamp of the redemption.
     * 
     * @return The timestamp of the last update to the redemption details.
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the last updated timestamp of the redemption.
     * 
     * @param updatedAt The new timestamp for the last update to the redemption details.
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}