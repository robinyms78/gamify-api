package sg.edu.ntu.gamify_demo.models;

import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;

@Entity
@Table(name = "rewards")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rewards {

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
    @JsonManagedReference("reward-redemptions")
    private List<RewardRedemption> redemptions;

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
