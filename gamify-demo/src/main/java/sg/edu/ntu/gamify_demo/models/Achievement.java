package sg.edu.ntu.gamify_demo.models;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @GeneratedValue(strategy = GenerationType.UUID) // For UUID in Hibernate 6+
    @Column(name = "achievement_id", updatable = false, nullable = false)
    private String achievementId;
    
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Type(JsonType.class)
    @Column(name = "criteria", columnDefinition = "jsonb", nullable = false)
    private JsonNode criteria;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private ZonedDateTime createdAt;
    
    /**
     * Constructs an Achievement with the provided details.
     * 
     * @param name The name of the achievement.
     * @param description The description of the achievement.
     * @param criteria The criteria for earning this achievement.
     */
    public Achievement(String name, String description, JsonNode criteria) {
        // Let Hibernate generate the ID
        this.name = name;
        this.description = description;
        this.criteria = criteria;
    }
}
