package sg.edu.ntu.gamify_demo.models;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The LadderLevel class represents a level in the gamification ladder system.
 * It defines the points required to reach each level and provides a human-readable label.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ladder_levels")
public class LadderLevel {
    @Id
    @Column(name = "level")
    private int level;
    
    @Column(name = "label", nullable = false)
    private String label;
    
    @Column(name = "points_required", nullable = false)
    private int pointsRequired;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * Constructs a LadderLevel with the provided details.
     * 
     * @param level The numeric level identifier.
     * @param label The human-readable label for this level.
     * @param pointsRequired The points required to reach this level.
     */
    public LadderLevel(int level, String label, int pointsRequired) {
        this.level = level;
        this.label = label;
        this.pointsRequired = pointsRequired;
    }
}
