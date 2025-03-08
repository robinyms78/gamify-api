package sg.edu.ntu.gamify_demo.models;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sg.edu.ntu.gamify_demo.models.enums.RewardStatus;

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
@Table(name = "reward_redemptions")
public class Redemption {
    @Id
    @Column(name = "redemption_id")
    private String redemptionId;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "reward_id", nullable = false)
    private Reward reward;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RewardStatus status;
    
    @Column(name = "redeemed_at")
    private LocalDateTime redeemedAt;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Constructs a Redemption object with the provided details.
     * 
     * @param user The user redeeming the reward.
     * @param reward The reward being redeemed.
     * @param status The status of the redemption.
     */
    public Redemption(User user, Reward reward, RewardStatus status) {
        this.redemptionId = UUID.randomUUID().toString();
        this.user = user;
        this.reward = reward;
        this.status = status;
    }
    
    /**
     * Marks this redemption as completed with the current timestamp.
     */
    public void complete() {
        this.status = RewardStatus.COMPLETED;
        this.redeemedAt = LocalDateTime.now();
    }
    
    /**
     * Marks this redemption as cancelled.
     */
    public void cancel() {
        this.status = RewardStatus.CANCELLED;
    }
}
