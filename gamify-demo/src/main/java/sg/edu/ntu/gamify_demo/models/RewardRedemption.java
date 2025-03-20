package sg.edu.ntu.gamify_demo.models;

import java.time.ZonedDateTime;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The Redemption class represents a reward redemption by a user.
 * It stores the redemption details including status, timestamps, and relationships to user and reward.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "redemptions")
public class RewardRedemption {

    @Id
    @Column(name = "id")
    private String id;
    
    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @JsonBackReference
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @JsonIgnoreProperties("redemption")
    @ManyToOne(optional = false)
    @JoinColumn(name = "reward_id", referencedColumnName = "id")
    private Rewards reward;

    /**
     * Constructs a Redemption object with required details.
     * 
     * @param user The user who made the redemption.
     * @param reward The reward that was redeemed.
     * @param status The redemption status.
     * @param createdAt The timestamp of when the redemption was created.
     * @param updatedAt The timestamp of when the redemption was last updated.
     */
    public RewardRedemption(User user, Rewards reward, String status) {
        this.id = UUID.randomUUID().toString();
        this.user = user;
        this.reward = reward;
        this.status = status;
        this.createdAt = ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();
    }
    
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
