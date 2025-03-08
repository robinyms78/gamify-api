package sg.edu.ntu.gamify_demo.models;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The Reward class represents a reward that users can redeem with their points.
 * It stores the reward details including cost, availability, and descriptions.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "rewards")
public class Reward {
    @Id
    @Column(name = "reward_id")
    private String rewardId;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "cost_in_points", nullable = false)
    private int costInPoints;
    
    @Column(name = "available")
    private boolean available;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Constructs a Reward object with the provided details.
     * 
     * @param name The name of the reward.
     * @param description The description of the reward.
     * @param costInPoints The points required to redeem this reward.
     * @param available Whether this reward is currently available.
     */
    public Reward(String name, String description, int costInPoints, boolean available) {
        this.rewardId = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.costInPoints = costInPoints;
        this.available = available;
    }
}
