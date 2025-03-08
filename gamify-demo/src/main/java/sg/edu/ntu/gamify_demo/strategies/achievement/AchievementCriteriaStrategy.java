package sg.edu.ntu.gamify_demo.strategies.achievement;

import com.fasterxml.jackson.databind.JsonNode;
import sg.edu.ntu.gamify_demo.models.User;

/**
 * Strategy interface for evaluating different types of achievement criteria.
 * Each implementation handles a specific type of achievement criteria.
 */
public interface AchievementCriteriaStrategy {
    
    /**
     * Evaluates whether a user meets the specified criteria for an achievement.
     * 
     * @param user The user to evaluate.
     * @param criteria The criteria to evaluate against.
     * @return true if the user meets the criteria, false otherwise.
     */
    boolean evaluate(User user, JsonNode criteria);
}
