package sg.edu.ntu.gamify_demo.services;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sg.edu.ntu.gamify_demo.interfaces.AchievementService;
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.models.LadderLevel;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.UserLadderStatus;
import sg.edu.ntu.gamify_demo.repositories.LadderLevelRepository;
import sg.edu.ntu.gamify_demo.repositories.UserLadderStatusRepository;

/**
 * Service for managing user progression through ladder levels.
 */
@Service
public class LadderService {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AchievementService achievementService;
    
    @Autowired
    private LadderLevelRepository ladderLevelRepository;
    
    @Autowired
    private UserLadderStatusRepository userLadderStatusRepository;
    
    /**
     * Get all ladder levels.
     * 
     * @return A map of level numbers to points required.
     */
    public Map<Long, Long> getLadderLevels() {
        List<LadderLevel> levels = ladderLevelRepository.findAll();
        Map<Long, Long> ladderLevels = new HashMap<>();
        
        for (LadderLevel level : levels) {
            ladderLevels.put(level.getLevel(), level.getPointsRequired());
        }
        
        return ladderLevels;
    }
    
    /**
     * Get a user's current ladder status.
     * 
     * @param userId The ID of the user.
     * @return The user's ladder status.
     */
    public UserLadderStatus getUserLadderStatus(String userId) {
        Optional<UserLadderStatus> status = userLadderStatusRepository.findById(userId);
        
        if (status.isPresent()) {
            return status.get();
        }
        
        // If the user doesn't have a ladder status yet, create one
        User user = userService.getUserById(userId);
        
        if (user == null) {
            return null;
        }
        
        return initializeUserLadderStatus(user);
    }
    
    /**
     * Initialize a new user's ladder status.
     * 
     * @param user The user to initialize.
     * @return The initialized ladder status.
     */
    @Transactional
    public UserLadderStatus initializeUserLadderStatus(User user) {
        // Get the first ladder level
        LadderLevel firstLevel = ladderLevelRepository.findByLevel(1);
        
        if (firstLevel == null) {
            // Create a default first level if none exists
            firstLevel = new LadderLevel();
            firstLevel.setLevel(1L);
            firstLevel.setLabel("Beginner");
            firstLevel.setPointsRequired(0L);
            firstLevel.setCreatedAt(ZonedDateTime.now());
            firstLevel = ladderLevelRepository.save(firstLevel);
        }
        
        // Create a new ladder status for the user
        UserLadderStatus status = new UserLadderStatus();
        status.setUser(user);
        status.setCurrentLevel(firstLevel);
        status.setEarnedPoints(user.getEarnedPoints());
        
        // Calculate points to next level
        LadderLevel nextLevel = ladderLevelRepository.findByLevel(firstLevel.getLevel().intValue() + 1);
        Long pointsToNextLevel = nextLevel != null ? 
                nextLevel.getPointsRequired() - user.getEarnedPoints() : 
                Long.MAX_VALUE;
        
        status.setPointsToNextLevel(Math.max(0L, pointsToNextLevel));
        status.setUpdatedAt(ZonedDateTime.now());
        
        return userLadderStatusRepository.save(status);
    }
    
    /**
     * Update a user's ladder status based on their earned points.
     * 
     * @param userId The ID of the user.
     * @return The updated ladder status.
     */
    @Transactional
    public UserLadderStatus updateUserLadderStatus(String userId) {
        User user = userService.getUserById(userId);
        
        if (user == null) {
            return null;
        }
        
        UserLadderStatus status = getUserLadderStatus(userId);
        
        if (status == null) {
            return null;
        }
        
        // Update earned points
        status.setEarnedPoints(user.getEarnedPoints());
        
        // Check if the user has leveled up
        List<LadderLevel> levels = ladderLevelRepository.findAllByOrderByLevelAsc();
        LadderLevel currentLevel = status.getCurrentLevel();
        LadderLevel newLevel = currentLevel;
        
        for (LadderLevel level : levels) {
            if (level.getLevel() > currentLevel.getLevel() && user.getEarnedPoints() >= level.getPointsRequired()) {
                newLevel = level;
            }
        }
        
        // Update level if changed
        if (newLevel.getLevel() != currentLevel.getLevel()) {
            status.setCurrentLevel(newLevel);
        }
        
        // Calculate points to next level
        LadderLevel nextLevel = null;
        for (LadderLevel level : levels) {
            if (level.getLevel() > newLevel.getLevel()) {
                nextLevel = level;
                break;
            }
        }
        
        Long pointsToNextLevel = nextLevel != null ? 
                nextLevel.getPointsRequired() - user.getEarnedPoints() : 
                Long.MAX_VALUE;
        
        status.setPointsToNextLevel(Math.max(0L, pointsToNextLevel));
        status.setUpdatedAt(ZonedDateTime.now());
        
        return userLadderStatusRepository.save(status);
    }
    
    /**
     * Get the label for a specific ladder level.
     * 
     * @param level The level number.
     * @return The label for the level.
     */
    public String getLevelLabel(int level) {
        LadderLevel ladderLevel = ladderLevelRepository.findByLevel(level);
        return ladderLevel != null ? ladderLevel.getLabel() : "Unknown";
    }
    
    /**
     * Create a new ladder level.
     * 
     * @param level The level number.
     * @param label The label for the level.
     * @param pointsRequired The points required to reach this level.
     * @return The created ladder level.
     */
    @Transactional
    public LadderLevel createLadderLevel(int level, String label, int pointsRequired) {
        LadderLevel ladderLevel = new LadderLevel();
        ladderLevel.setLevel((long)level);
        ladderLevel.setLabel(label);
        ladderLevel.setPointsRequired((long)pointsRequired);
        ladderLevel.setCreatedAt(ZonedDateTime.now());
        
        return ladderLevelRepository.save(ladderLevel);
    }
    
    /**
     * Update an existing ladder level.
     * 
     * @param level The level number to update.
     * @param label The new label for the level.
     * @param pointsRequired The new points required to reach this level.
     * @return The updated ladder level.
     */
    @Transactional
    public LadderLevel updateLadderLevel(int level, String label, int pointsRequired) {
        LadderLevel ladderLevel = ladderLevelRepository.findByLevel(level);
        
        if (ladderLevel == null) {
            return null;
        }
        
        ladderLevel.setLabel(label);
        ladderLevel.setPointsRequired((long)pointsRequired);
        
        return ladderLevelRepository.save(ladderLevel);
    }
    
    /**
     * Delete a ladder level.
     * 
     * @param level The level number to delete.
     * @return True if the level was deleted, false otherwise.
     */
    @Transactional
    public boolean deleteLadderLevel(int level) {
        LadderLevel ladderLevel = ladderLevelRepository.findByLevel(level);
        
        if (ladderLevel == null) {
            return false;
        }
        
        // Check if any users are at this level
        if (userLadderStatusRepository.existsByCurrentLevel(ladderLevel)) {
            return false;
        }
        
        ladderLevelRepository.delete(ladderLevel);
        return true;
    }
}
