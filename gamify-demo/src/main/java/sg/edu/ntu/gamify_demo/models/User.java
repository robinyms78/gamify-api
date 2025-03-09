package sg.edu.ntu.gamify_demo.models;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sg.edu.ntu.gamify_demo.models.enums.UserRole;

/**
 * The User class represents a user in the system.
 * It stores user details including credentials, role, department, and points.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "department")
    private String department;

    @Column(name = "earned_points", nullable = false)
    private int earnedPoints;

    @Column(name = "available_points", nullable = false)
    private int availablePoints;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private UserLadderStatus ladderStatus;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Leaderboard leaderboard;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PointsTransaction> pointsTransactions = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TaskEvent> taskEvents = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Redemption> redemptions = new ArrayList<>();

    /**
     * Initializes default values for new users
     */
    @PrePersist
    protected void onCreate() {
        if (earnedPoints == 0) {
            earnedPoints = 0;
        }
        if (availablePoints == 0) {
            availablePoints = 0;
        }
    }
    
    /**
     * Helper method to add a points transaction and update points.
     * 
     * @param transaction The points transaction to add.
     */
    public void addPointsTransaction(PointsTransaction transaction) {
        pointsTransactions.add(transaction);
        // Update points based on transaction type
        if (transaction.getPoints() > 0) {
            // Earning points
            this.earnedPoints += transaction.getPoints();
            this.availablePoints += transaction.getPoints();
        } else {
            // Spending points (redemption)
            this.availablePoints += transaction.getPoints(); // Will subtract since points is negative
        }
    }
    
    /**
     * Helper method to add a task event.
     * 
     * @param taskEvent The task event to add.
     */
    public void addTaskEvent(TaskEvent taskEvent) {
        taskEvents.add(taskEvent);
    }
    
    /**
     * Helper method to add a redemption.
     * 
     * @param redemption The redemption to add.
     */
    public void addRedemption(Redemption redemption) {
        redemptions.add(redemption);
    }
    
    /**
     * Helper method to set the user's ladder status and maintain bidirectional relationship.
     * 
     * @param ladderStatus The ladder status to set.
     */
    public void setLadderStatus(UserLadderStatus ladderStatus) {
        this.ladderStatus = ladderStatus;
        if (ladderStatus != null && ladderStatus.getUser() != this) {
            ladderStatus.setUser(this);
        }
    }
    
    /**
     * Helper method to set the user's leaderboard entry and maintain bidirectional relationship.
     * 
     * @param leaderboard The leaderboard entry to set.
     */
    public void setLeaderboardEntry(Leaderboard leaderboard) {
        this.leaderboard = leaderboard;
        if (leaderboard != null && leaderboard.getUser() != this) {
            leaderboard.setUser(this);
            leaderboard.syncWithUser(); // Ensure leaderboard data is in sync
        }
    }
}
