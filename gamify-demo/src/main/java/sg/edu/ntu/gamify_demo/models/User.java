package sg.edu.ntu.gamify_demo.models;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonManagedReference;

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
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
@Schema(description = "System user entity")
public class User {
    @Id
    @Column(name = "id")
    @Schema(description = "Unique user ID", 
            example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    @Column(name = "username", unique = true, nullable = false)
    @Schema(description = "Unique username", 
            example = "tech_guru_42")
    private String username;

    @Column(name = "email", unique = true, nullable = false)
    @Schema(description = "User email address", 
            example = "user@company.com")
    private String email;

    @Column(name = "password_hash", nullable = false)
    @Schema(hidden = true) // Hide sensitive field
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Schema(description = "User role", 
            implementation = UserRole.class,
            example = "employee")
    private UserRole role;

    @Column(name = "department")
    @Schema(description = "Department name", 
            example = "Quality Assurance")
    private String department;

    @Column(name = "earned_points", nullable = false)
    @Schema(description = "Lifetime earned points", 
            example = "1500")
    private Long earnedPoints;

    @Column(name = "available_points", nullable = false)
    @Schema(description = "Currently available points", 
            example = "750")
    private Long availablePoints;

    @CreationTimestamp
    @Column(name = "created_at")
    @Schema(hidden = true)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @Schema(hidden = true)
    private ZonedDateTime updatedAt;
    
    @OneToOne(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Schema(hidden = true) // Hide complex relationships
    private UserLadderStatus ladderStatus;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Schema(hidden = true)
    private Leaderboard leaderboard;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonManagedReference
    @Schema(hidden = true)
    private List<PointsTransaction> pointsTransactions = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonManagedReference
    @Schema(hidden = true)
    private List<TaskEvent> taskEvents = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonManagedReference
    @Schema(hidden = true)
    private List<RewardRedemption> redemptions = new ArrayList<>();

    /**
     * Initializes default values for new users
     */
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (earnedPoints == null) {
            earnedPoints = 0L;
        }
        if (availablePoints == null) {
            availablePoints = 0L;
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
     * Helper method to add a redemption and maintain bidirectional relationship.
     * 
     * @param redemption The redemption to add.
     */
    public void addRedemption(RewardRedemption redemption) {
        redemptions.add(redemption);
        if (redemption.getUser() != this) {
            redemption.setUser(this);
        }
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
