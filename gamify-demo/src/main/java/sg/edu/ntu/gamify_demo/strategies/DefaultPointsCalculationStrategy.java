package sg.edu.ntu.gamify_demo.strategies;

import org.springframework.stereotype.Component;

import sg.edu.ntu.gamify_demo.models.LadderLevel;

/**
 * Default implementation of the PointsCalculationStrategy.
 * Calculates points to next level based on the difference between 
 * the next level's required points and the user's current points.
 */
@Component
public class DefaultPointsCalculationStrategy implements PointsCalculationStrategy {

    @Override
    public int calculatePointsToNextLevel(int currentPoints, LadderLevel currentLevel, LadderLevel nextLevel) {
        if (nextLevel == null) {
            // User is at the maximum level
            return 0;
        }
        
        int pointsToNextLevel = nextLevel.getPointsRequired() - currentPoints;
        
        // Ensure we don't return a negative value
        return Math.max(0, pointsToNextLevel);
    }
}
