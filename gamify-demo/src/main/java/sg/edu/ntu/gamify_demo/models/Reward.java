// Java class for Reward
// Reward.java

package sg.edu.ntu.gamify_demo.models;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * The Reward class represents a class which rewards the user in the system.
 * It stores the reward details including rewardId and timestamps for creation and updates.
 */

@Setter 
@Getter

public class Reward {
    // Create instance of user class 
    User user = new User();

    // Instance variables
    private int costInPoints;
    protected final String rewardId;
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

    public Reward(String name, String description, int costInPoints,  boolean available) 
    {
        user.userId = UUID.randomUUID().toString();
        this.rewardId = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.costInPoints = costInPoints;
        this.available = available;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}