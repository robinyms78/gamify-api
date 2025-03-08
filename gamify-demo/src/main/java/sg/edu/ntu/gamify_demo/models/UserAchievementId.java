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
    
    private String user;
    private String achievement;
}
