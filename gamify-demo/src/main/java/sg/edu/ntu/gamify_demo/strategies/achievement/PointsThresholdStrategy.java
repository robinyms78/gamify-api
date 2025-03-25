package sg.edu.ntu.gamify_demo.strategies.achievement;

import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import sg.edu.ntu.gamify_demo.models.User;

/**
 * Strategy implementation for evaluating points threshold achievement criteria.
 * This strategy checks if a user has earned enough points to meet the threshold.
 */
@Component
public class PointsThresholdStrategy implements AchievementCriteriaStrategy {

    /**
     * Evaluates whether a user has earned enough points to meet the threshold.
     * 
     * @param user The user to evaluate.
     * @param criteria The criteria containing the points threshold.
     * @return true if the user's earned points meet or exceed the threshold, false otherwise.
     */
    @Override
    public boolean evaluate(User user, JsonNode criteria) {
        if (criteria == null || !criteria.has("threshold")) {
            return false;
        }
        
        int threshold = criteria.get("threshold").asInt();
        return user.getEarnedPoints() >= threshold;
    }
}
