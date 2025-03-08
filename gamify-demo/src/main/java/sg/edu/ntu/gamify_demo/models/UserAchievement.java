package sg.edu.ntu.gamify_demo.models;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The UserAchievement class represents an achievement earned by a user.
 * It links users to their earned achievements and stores metadata about the achievement.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@IdClass(UserAchievementId.class)
@Table(name = "user_achievements")
public class UserAchievement {
    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private User user;
    
    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "achievement_id", nullable = false, referencedColumnName = "achievement_id")
    private Achievement achievement;
    
    @Column(name = "earned_at")
    private LocalDateTime earnedAt;
    
    @Type(JsonType.class)
    @Column(name = "metadata", columnDefinition = "json")
    private JsonNode metadata;
    
    /**
     * Constructs a UserAchievement with the provided details.
     * 
     * @param user The user who earned the achievement.
     * @param achievement The achievement that was earned.
     * @param metadata Additional data about how the achievement was earned.
     */
    public UserAchievement(User user, Achievement achievement, JsonNode metadata) {
        this.user = user;
        this.achievement = achievement;
        this.earnedAt = LocalDateTime.now();
        this.metadata = metadata;
    }
}
