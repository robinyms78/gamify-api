package sg.edu.ntu.gamify_demo.models;

import java.time.ZonedDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The Leaderboard class represents a user's ranking in the system.
 * It stores the user's earned points, level, and rank for leaderboard display.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "leaderboard")
public class Leaderboard {
@Id
@Column(name = "user_id")
private String userId;

@OneToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "user_id", insertable = false, updatable = false)
@JsonBackReference
private User user;
    
    @Column(name = "username", nullable = false)
    private String username;
    
    @Column(name = "department")
    private String department;
    
    @Column(name = "earned_points", nullable = false)
    private Long earnedPoints;
    
    @ManyToOne
    @JoinColumn(name = "current_level", nullable = false)
    private LadderLevel currentLevel;
    
    @Column(name = "rank", nullable = false)
    private Long rank;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private ZonedDateTime createdAt;
    
    /**
     * Constructs a Leaderboard entry with the provided details.
     * 
     * @param user The user this leaderboard entry represents.
     * @param earnedPoints The total points earned by the user.
     * @param currentLevel The user's current level.
     * @param rank The user's rank on the leaderboard.
     */
    public Leaderboard(User user, Long earnedPoints, LadderLevel currentLevel, Long rank) {
        this.user = user;
        this.userId = user.getId(); // Set the ID from the user
        this.username = user.getUsername();
        this.department = user.getDepartment();
        this.earnedPoints = earnedPoints;
        this.currentLevel = currentLevel;
        this.rank = rank;
    }
    
    /**
     * Ensures the userId is set from the user.
     * This method is called before persisting the entity.
     */
    @jakarta.persistence.PrePersist
    protected void onCreate() {
        if (this.userId == null && this.user != null) {
            this.userId = this.user.getId();
        }
    }
    
    /**
     * Additional check before update to ensure userId is always set.
     */
    @jakarta.persistence.PreUpdate
    protected void onUpdate() {
        if (this.userId == null && this.user != null) {
            this.userId = this.user.getId();
        }
    }
    
    /**
     * Updates the user's rank on the leaderboard.
     * 
     * @param newRank The new rank for the user.
     */
    public void updateRank(Long newRank) {
        this.rank = newRank;
    }
    
    /**
     * Synchronizes this leaderboard entry with the associated user's data.
     * This method should be called whenever user data changes to maintain consistency.
     */
    public void syncWithUser() {
        if (this.user != null) {
            this.username = user.getUsername();
            this.department = user.getDepartment();
            this.earnedPoints = user.getEarnedPoints();
        }
    }
    
    /**
     * Updates the user's level and earned points on the leaderboard.
     * 
     * @param level The user's current level.
     * @param earnedPoints The total points earned by the user.
     */
    public void updateLevelAndPoints(LadderLevel level, Long earnedPoints) {
        this.currentLevel = level;
        this.earnedPoints = earnedPoints;
        // Also sync other user data to ensure consistency
        syncWithUser();
    }
}
