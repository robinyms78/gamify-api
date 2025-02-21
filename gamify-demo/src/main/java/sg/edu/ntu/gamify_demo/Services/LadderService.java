// Java class for LadderService
// LadderService.java

package sg.edu.ntu.gamify_demo.Services;

import java.util.HashMap;

public class LadderService {
    private String userId;
    private int currentLevel;
    private int points;
    private int pointsToNextLevel;
    private String[] achievements;
    private HashMap<Integer, Integer> ladderLevels;

    // Constructor
    public LadderService() {
    }

    public LadderService(int level, int pointsRequired) {
        this.currentLevel = level;
        this.pointsToNextLevel = pointsRequired;
    }

    // Methods
    public HashMap<Integer, Integer> getLadderLevels() {
        return ladderLevels;
    }

}
