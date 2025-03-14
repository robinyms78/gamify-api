package sg.edu.ntu.gamify_demo.strategies.achievement;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import sg.edu.ntu.gamify_demo.models.User;

/**
 * Evaluator for achievement criteria that uses the Strategy pattern.
 * This class manages different achievement criteria strategies and delegates
 * the evaluation to the appropriate strategy based on the criteria type.
 */
@Component
public final class AchievementCriteriaEvaluator {

    private final Map<String, AchievementCriteriaStrategy> strategies = new HashMap<>();
    
    /**
     * Constructor for dependency injection.
     * Registers all available achievement criteria strategies.
     * 
     * @param pointsThresholdStrategy Strategy for points threshold criteria.
     * @param taskCompletionStrategy Strategy for task completion criteria.
     * @param consecutiveDaysStrategy Strategy for consecutive days criteria.
     */
    public AchievementCriteriaEvaluator(
            PointsThresholdStrategy pointsThresholdStrategy,
            TaskCompletionStrategy taskCompletionStrategy,
            ConsecutiveDaysStrategy consecutiveDaysStrategy) {
        
        // Register strategies
        registerStrategy("POINTS_THRESHOLD", pointsThresholdStrategy);
        registerStrategy("TASK_COMPLETION_COUNT", taskCompletionStrategy);
        registerStrategy("CONSECUTIVE_DAYS", consecutiveDaysStrategy);
    }
    
    /**
     * Registers a strategy for a specific criteria type.
     * 
     * @param type The criteria type.
     * @param strategy The strategy to register.
     */
    public void registerStrategy(String type, AchievementCriteriaStrategy strategy) {
        strategies.put(type, strategy);
    }
    
    /**
     * Evaluates whether a user meets the criteria for an achievement.
     * 
     * @param user The user to evaluate.
     * @param criteria The criteria to evaluate against.
     * @return true if the user meets the criteria, false otherwise.
     */
    public boolean evaluateCriteria(User user, JsonNode criteria) {
        if (criteria == null || !criteria.has("type")) {
            return false;
        }
        
        String type = criteria.get("type").asText();
        AchievementCriteriaStrategy strategy = strategies.get(type);
        
        if (strategy == null) {
            return false;
        }
        
        return strategy.evaluate(user, criteria);
    }
}
