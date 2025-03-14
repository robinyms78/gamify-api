package sg.edu.ntu.gamify_demo.models;

import java.time.LocalDateTime;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "costInPoints")
    private int costInPoints;

    @Column(name = "available")
    private boolean available;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
}
