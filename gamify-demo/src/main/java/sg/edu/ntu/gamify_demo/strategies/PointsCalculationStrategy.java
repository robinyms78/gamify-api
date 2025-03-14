package sg.edu.ntu.gamify_demo.strategies;

import sg.edu.ntu.gamify_demo.models.LadderLevel;

/**
 * Strategy interface for calculating points to the next level.
 * Allows for different implementations of points calculation logic.
 */
public interface PointsCalculationStrategy {
    
    /**
     * Calculate the points needed to reach the next level.
     * 
     * @param currentPoints The user's current earned points.
     * @param currentLevel The user's current ladder level.
     * @param nextLevel The next ladder level, or null if at max level.
     * @return The points needed to reach the next level, or 0 if at max level.
     */
    long calculatePointsToNextLevel(long currentPoints, LadderLevel currentLevel, LadderLevel nextLevel);
}
