package sg.edu.ntu.gamify_demo.models;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The Achievement class represents an achievement that users can earn in the system.
 * It defines the criteria and description for each achievement.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "achievements")
public class Achievement {
    @Id
    @Column(name = "achievement_id")
    @Schema(description = "The unique identifier of the achievement", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    private String achievementId;
    
    @Column(name = "name", nullable = false, unique = true)
    @Schema(description = "The name of the achievement", example = "Task Master")
    private String name;
    
    @Column(name = "description")
    @Schema(description = "A description of the achievement", example = "Complete 100 tasks.")
    private String description;
    
    @Type(JsonType.class)
    @Column(name = "criteria", columnDefinition = "json")
    @Schema(description = "The criteria for earning this achievement (JSON format)", example = "{\"type\": \"TASK_COMPLETION\", \"count\": 100}")
    private JsonNode criteria;
    
    @CreationTimestamp
    @Column(name = "created_at")
    @Schema(description = "The date and time when the achievement was created")
    private LocalDateTime createdAt;
    
    /**
     * Constructs an Achievement with the provided details.
     * 
     * @param name The name of the achievement.
     * @param description The description of the achievement.
     * @param criteria The criteria for earning this achievement.
     */
    public Achievement(String name, String description, JsonNode criteria) {
        this.achievementId = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.criteria = criteria;
    }
}
