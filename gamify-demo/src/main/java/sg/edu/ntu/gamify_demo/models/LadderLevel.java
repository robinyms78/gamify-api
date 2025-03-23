package sg.edu.ntu.gamify_demo.models;

import java.time.ZonedDateTime;

import org.hibernate.annotations.CreationTimestamp;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "LadderLevel", description = "Represents a level in the gamification ladder system")
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
    @Schema(description = "Numeric level identifier", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long level;
    
    @Column(name = "label", nullable = false)
    @Schema(description = "Human-readable label for this level", example = "Seasoned Adventurer", requiredMode = Schema.RequiredMode.REQUIRED)
    private String label;
    
    @Column(name = "points_required", nullable = false)
    @Schema(description = "Points required to reach this level", example = "600", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long pointsRequired;
    
    @CreationTimestamp
    @Column(name = "created_at")
    @Schema(description = "Timestamp when this level was created", example = "2024-03-15T14:30:45Z")
    private ZonedDateTime createdAt;
    
    /**
     * Constructs a LadderLevel with the provided details.
     * 
     * @param level The numeric level identifier.
     * @param label The human-readable label for this level.
     * @param pointsRequired The points required to reach this level.
     */
    public LadderLevel(Long level, String label, Long pointsRequired) {
        this.level = level;
        this.label = label;
        this.pointsRequired = pointsRequired;
    }
}
