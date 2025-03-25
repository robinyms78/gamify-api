package sg.edu.ntu.gamify_demo.strategies.achievement;

import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.UserLadderStatus;

/**
 * Strategy implementation for evaluating level-based achievement criteria.
 * This strategy checks if a user has reached a specific level in the gamification ladder.
 */
@Component
public class LevelBasedStrategy implements AchievementCriteriaStrategy {

    /**
     * Evaluates whether a user has reached the required level.
     * 
     * @param user The user to evaluate.
     * @param criteria The criteria containing the required level.
     * @return true if the user's level meets or exceeds the required level, false otherwise.
     */
    @Override
    public boolean evaluate(User user, JsonNode criteria) {
        if (criteria == null || !criteria.has("requiredLevel")) {
            return false;
        }
        
        int requiredLevel = criteria.get("requiredLevel").asInt();
        
        // Get the user's ladder status
        UserLadderStatus ladderStatus = user.getLadderStatus();
        if (ladderStatus == null) {
            return false;
        }
        
        // Check if the user has reached the required level
        Long currentLevel = ladderStatus.getCurrentLevel().getLevel();
        return currentLevel >= requiredLevel;
    }
}
