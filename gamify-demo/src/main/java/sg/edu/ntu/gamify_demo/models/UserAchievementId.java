package sg.edu.ntu.gamify_demo.models;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Composite key class for UserAchievement.
 * This class represents the composite primary key for the user_achievements table,
 * which consists of user_id and achievement_id.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserAchievementId implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Field names must match the property names in UserAchievement entity
    private User user; // Maps to UserAchievement.user
    private Achievement achievement; // Maps to UserAchievement.achievement
    
    // Override equals and hashCode to use the IDs of the entities
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        UserAchievementId that = (UserAchievementId) o;
        
        if (user != null ? !user.getId().equals(that.user != null ? that.user.getId() : null) : that.user != null) return false;
        return achievement != null ? achievement.getAchievementId().equals(that.achievement != null ? that.achievement.getAchievementId() : null) : that.achievement == null;
    }
    
    @Override
    public int hashCode() {
        int result = user != null ? user.getId().hashCode() : 0;
        result = 31 * result + (achievement != null ? achievement.getAchievementId().hashCode() : 0);
        return result;
    }
}
