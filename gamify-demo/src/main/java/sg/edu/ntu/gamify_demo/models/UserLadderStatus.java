package sg.edu.ntu.gamify_demo.models;

import java.time.ZonedDateTime;

import org.hibernate.annotations.UpdateTimestamp;

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
 * The UserLadderStatus class represents a user's current status in the ladder system.
 * It tracks the user's current level, earned points, and points needed for the next level.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_ladder_status")
public class UserLadderStatus {
@Id
@Column(name = "id")
private String id;

@OneToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
@JsonBackReference
private User user;
    
    @ManyToOne
    @JoinColumn(name = "current_level", nullable = false)
    private LadderLevel currentLevel;
    
    @Column(name = "earned_points", nullable = false)
    private Long earnedPoints;
    
    @Column(name = "points_to_next_level", nullable = false)
    private Long pointsToNextLevel;
    
    @Column(name = "achievements")
    private String achievements;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;
    
    /**
     * Constructs a UserLadderStatus with the provided details.
     * 
     * @param user The user whose ladder status this represents.
     * @param currentLevel The user's current level in the ladder.
     * @param earnedPoints The total points the user has earned.
     * @param pointsToNextLevel The points needed to reach the next level.
     */
    public UserLadderStatus(User user, LadderLevel currentLevel, Long earnedPoints, Long pointsToNextLevel) {
        this.user = user;
        this.id = user.getId(); // Set the ID from the user
        this.currentLevel = currentLevel;
        this.earnedPoints = earnedPoints;
        this.pointsToNextLevel = pointsToNextLevel;
    }
    
    /**
     * Ensures the ID is set from the user.
     * This method is called before persisting the entity.
     */
    @jakarta.persistence.PrePersist
    protected void onCreate() {
        if (this.id == null && this.user != null) {
            this.id = this.user.getId();
            // Add logging for debugging
            System.out.println("Setting UserLadderStatus ID from user: " + this.id);
        }
    }
    
    /**
     * Additional check before update to ensure ID is always set.
     */
    @jakarta.persistence.PreUpdate
    protected void onUpdate() {
        if (this.id == null && this.user != null) {
            this.id = this.user.getId();
            System.out.println("Setting UserLadderStatus ID from user during update: " + this.id);
        }
    }
    
    /**
     * Updates the user's ladder status based on new points.
     * 
     * @param newPoints The new total points earned by the user.
     * @param nextLevel The next level in the ladder system.
     * @param nextNextLevel The level after the next level, or null if nextLevel is the highest level.
     * @return true if the user leveled up, false otherwise.
     */
    public boolean updatePoints(Long newPoints, LadderLevel nextLevel, LadderLevel nextNextLevel) {
        boolean leveledUp = false;
        this.earnedPoints = newPoints;
        
        if (nextLevel != null && newPoints >= nextLevel.getPointsRequired()) {
            this.currentLevel = nextLevel;
            leveledUp = true;
            
            // Calculate points to next level if there is one
            if (nextNextLevel != null) {
                this.pointsToNextLevel = nextNextLevel.getPointsRequired() - newPoints;
            } else {
                this.pointsToNextLevel = 0L; // Max level reached
            }
        } else if (nextLevel != null) {
            this.pointsToNextLevel = nextLevel.getPointsRequired() - newPoints;
        }
        
        return leveledUp;
    }
}
