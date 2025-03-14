package sg.edu.ntu.gamify_demo.models;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * The Reward class represents a reward that users can redeem with their points.
 * It stores the reward details including cost, availability, and descriptions.
 */

@Setter 
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Reward {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "cost_in_points")
    private Long costInPoints;

    @Column(name = "available")
    private boolean available;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @OneToMany(mappedBy = "reward")
    private List<Redemption> redemptions;

    /**
     * Constructs a Reward object with the provided details.
     * @param reward Id The unique identifier for the reward.
     * @param name The name of the reward.
     * @param description The description of the reward.
     * @param costInpoints The points associated with the reward.
     * @param available The availability of the reward.
     * @param createdAt The timestamp of when the reward was created.
     * @param updatedAt The timestamp of when the reward was last updated.
     */
     
    @jakarta.persistence.PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = ZonedDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = ZonedDateTime.now();
        }
    }
}
