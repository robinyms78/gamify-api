package sg.edu.ntu.gamify_demo.models;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "username", nullable = false)
    private String username;
    
    @Column(name = "department")
    private String department;
    
    @Column(name = "earned_points", nullable = false)
    private int earnedPoints;
    
    @ManyToOne
    @JoinColumn(name = "current_level", nullable = false)
    private LadderLevel currentLevel;
    
    @Column(name = "rank", nullable = false)
    private int rank;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * Constructs a Leaderboard entry with the provided details.
     * 
     * @param user The user this leaderboard entry represents.
     * @param earnedPoints The total points earned by the user.
     * @param currentLevel The user's current level.
     * @param rank The user's rank on the leaderboard.
     */
    public Leaderboard(User user, int earnedPoints, LadderLevel currentLevel, int rank) {
        this.user = user;
        this.username = user.getUsername();
        this.department = user.getDepartment();
        this.earnedPoints = earnedPoints;
        this.currentLevel = currentLevel;
        this.rank = rank;
    }
    
    /**
     * Updates the user's rank on the leaderboard.
     * 
     * @param newRank The new rank for the user.
     */
    public void updateRank(int newRank) {
        this.rank = newRank;
    }
}
