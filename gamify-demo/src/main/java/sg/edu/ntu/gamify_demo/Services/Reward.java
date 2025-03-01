package sg.edu.ntu.gamify_demo.Services;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The Reward class represents a class which rewards the user in the system.
 * It stores the reward details including rewardId and timestamps for creation and updates.
 */

public class Reward {
    // Create instance of user class 
    User user = new User();

    // Instance variables
    private int costInPoints;
    protected String rewardId;
    private String name;
    private String description;
    private boolean available;
    private LocalDateTime createdAt, updatedAt;

    /**
     * Constructs a Reward object with the provided details.
     * 
     * @param user The instance of the User.
     * @param userId The unique identifier for the user.
     * @param rewardId The unique identifier for the reward.
     * @param costInpoints The points associated with the reward.
     * @param name The name of the reward.
     * @param description The description of the reward.
     * @param available The availability of the reward.
     * @param createdAt The timestamp of when the reward was created.
     * @param updatedAt The timestamp of when the reward was last updated.
     */

    public Reward() {
    }

    public Reward(String userId, int rewardId, int costInPoints, String name, String description, boolean available, LocalDateTime createdAt, LocalDateTime updatedAt) 
    {
        user.userId = userId;
        this.rewardId = UUID.randomUUID().toString();
        this.costInPoints = costInPoints;
        this.name = name;
        this.description = description;
        this.available = available;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Gets the instance of the User.
     * 
     * @return The instance of the User.
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
     * Gets the unique identifier of the reward.
     * 
     * @return The reward ID.
     */
    public String getRewardId() {
        return rewardId;
    }

    /**
     * Gets the points associated with the reward.
     * 
     * @return The reward points.
     */
    public int getCostInPoints() {
        return costInPoints;
    }

    /**
     * Sets the points associated with the reward.
     * 
     * @param costInPoints The reward points.
     */
    public void setCostInPoints(int costInPoints) {
        this.costInPoints = costInPoints;
    }

    /**
     * Gets the name of the reward.
     * 
     * @return The reward name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the reward.
     * 
     * @param name The reward name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description of the reward.
     * 
     * @return The reward description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the reward.
     * 
     * @param name The reward description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the availability of the reward.
     * 
     * @return The reward availability.
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Sets the availability of the reward.
     * 
     * @return The reward availability.
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }

    /**
     * Gets the creation timestamp of the reward.
     * 
     * @return The timestamp when the reward was created.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Sets the creation timestamp of the reward.
     * 
     * @param createdAt The new timestamp for when the reward was created.
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the last updated timestamp of the reward.
     * 
     * @return The timestamp of the last update to the reward's details.
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the last updated timestamp of the reward.
     * 
     * @param updatedAt The new timestamp for the last update to the reward's details.
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}