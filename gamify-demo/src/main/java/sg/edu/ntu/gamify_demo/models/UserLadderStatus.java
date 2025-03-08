package sg.edu.ntu.gamify_demo.models;

import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
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
    @Column(name = "user_id")
    private String id;
    
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "current_level", nullable = false)
    private LadderLevel currentLevel;
    
    @Column(name = "earned_points", nullable = false)
    private int earnedPoints;
    
    @Column(name = "points_to_next_level", nullable = false)
    private int pointsToNextLevel;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Constructs a UserLadderStatus with the provided details.
     * 
     * @param user The user whose ladder status this represents.
     * @param currentLevel The user's current level in the ladder.
     * @param earnedPoints The total points the user has earned.
     * @param pointsToNextLevel The points needed to reach the next level.
     */
    public UserLadderStatus(User user, LadderLevel currentLevel, int earnedPoints, int pointsToNextLevel) {
        this.user = user;
        this.id = user.getId(); // Set the ID from the user
        this.currentLevel = currentLevel;
        this.earnedPoints = earnedPoints;
        this.pointsToNextLevel = pointsToNextLevel;
    }
    
    /**
     * Updates the user's ladder status based on new points.
     * 
     * @param newPoints The new total points earned by the user.
     * @param nextLevel The next level in the ladder system.
     * @return true if the user leveled up, false otherwise.
     */
    public boolean updatePoints(int newPoints, LadderLevel nextLevel) {
        boolean leveledUp = false;
        this.earnedPoints = newPoints;
        
        if (nextLevel != null && newPoints >= nextLevel.getPointsRequired()) {
            this.currentLevel = nextLevel;
            leveledUp = true;
            
            // Calculate points to next level if there is one
            LadderLevel nextNextLevel = null; // This would need to be fetched from the repository
            if (nextNextLevel != null) {
                this.pointsToNextLevel = nextNextLevel.getPointsRequired() - newPoints;
            } else {
                this.pointsToNextLevel = 0; // Max level reached
            }
        } else if (nextLevel != null) {
            this.pointsToNextLevel = nextLevel.getPointsRequired() - newPoints;
        }
        
        return leveledUp;
    }
}
