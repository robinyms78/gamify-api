package sg.edu.ntu.gamify_demo.Services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sg.edu.ntu.gamify_demo.dtos.LadderStatusDTO;
import sg.edu.ntu.gamify_demo.interfaces.LadderStatusService;
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.models.LadderLevel;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.UserLadderStatus;
import sg.edu.ntu.gamify_demo.repositories.LadderLevelRepository;
import sg.edu.ntu.gamify_demo.repositories.UserLadderStatusRepository;
import sg.edu.ntu.gamify_demo.strategies.PointsCalculationStrategy;

/**
 * Implementation of the LadderStatusService interface.
 * Provides methods for retrieving and updating user ladder status.
 */
@Service
public class LadderStatusServiceImpl implements LadderStatusService {
    
    private final UserService userService;
    private final LadderLevelRepository ladderLevelRepository;
    private final UserLadderStatusRepository userLadderStatusRepository;
    private final PointsCalculationStrategy pointsCalculationStrategy;
    
    /**
     * Constructor for dependency injection.
     */
    public LadderStatusServiceImpl(
            UserService userService,
            LadderLevelRepository ladderLevelRepository,
            UserLadderStatusRepository userLadderStatusRepository,
            PointsCalculationStrategy pointsCalculationStrategy) {
        this.userService = userService;
        this.ladderLevelRepository = ladderLevelRepository;
        this.userLadderStatusRepository = userLadderStatusRepository;
        this.pointsCalculationStrategy = pointsCalculationStrategy;
    }
    
    @Override
    public LadderStatusDTO getUserLadderStatus(String userId) {
        UserLadderStatus status = getUserLadderStatusEntity(userId);
        
        if (status == null) {
            return null;
        }
        
        return convertToDTO(status);
    }
    
    @Override
    @Transactional
    public LadderStatusDTO updateUserLadderStatus(String userId) {
        User user = userService.getUserById(userId);
        
        if (user == null) {
            return null;
        }
        
        UserLadderStatus status = getUserLadderStatusEntity(userId);
        
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
        
        // Find the next level
        LadderLevel nextLevel = findNextLevel(levels, newLevel);
        
        // Calculate points to next level using the strategy
        int pointsToNextLevel = pointsCalculationStrategy.calculatePointsToNextLevel(
                user.getEarnedPoints(), newLevel, nextLevel);
        
        status.setPointsToNextLevel(pointsToNextLevel);
        status.setUpdatedAt(LocalDateTime.now());
        
        UserLadderStatus updatedStatus = userLadderStatusRepository.save(status);
        
        return convertToDTO(updatedStatus);
    }
    
    /**
     * Get a user's ladder status entity.
     * If the user doesn't have a ladder status yet, initialize one.
     * 
     * @param userId The ID of the user.
     * @return The user's ladder status entity, or null if the user doesn't exist.
     */
    private UserLadderStatus getUserLadderStatusEntity(String userId) {
        Optional<UserLadderStatus> statusOpt = userLadderStatusRepository.findById(userId);
        
        if (statusOpt.isPresent()) {
            return statusOpt.get();
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
    private UserLadderStatus initializeUserLadderStatus(User user) {
        // Get the first ladder level
        LadderLevel firstLevel = ladderLevelRepository.findByLevel(1);
        
        if (firstLevel == null) {
            // Create a default first level if none exists
            firstLevel = new LadderLevel();
            firstLevel.setLevel(1);
            firstLevel.setLabel("Beginner");
            firstLevel.setPointsRequired(0);
            firstLevel.setCreatedAt(LocalDateTime.now());
            firstLevel = ladderLevelRepository.save(firstLevel);
        }
        
        // Create a new ladder status for the user
        UserLadderStatus status = new UserLadderStatus();
        status.setUser(user);
        status.setCurrentLevel(firstLevel);
        status.setEarnedPoints(user.getEarnedPoints());
        
        // Find the next level
        List<LadderLevel> levels = ladderLevelRepository.findAllByOrderByLevelAsc();
        LadderLevel nextLevel = findNextLevel(levels, firstLevel);
        
        // Calculate points to next level using the strategy
        int pointsToNextLevel = pointsCalculationStrategy.calculatePointsToNextLevel(
                user.getEarnedPoints(), firstLevel, nextLevel);
        
        status.setPointsToNextLevel(pointsToNextLevel);
        status.setUpdatedAt(LocalDateTime.now());
        
        return userLadderStatusRepository.save(status);
    }
    
    /**
     * Find the next level after the current level.
     * 
     * @param levels All ladder levels, ordered by level.
     * @param currentLevel The current level.
     * @return The next level, or null if at max level.
     */
    private LadderLevel findNextLevel(List<LadderLevel> levels, LadderLevel currentLevel) {
        for (LadderLevel level : levels) {
            if (level.getLevel() > currentLevel.getLevel()) {
                return level;
            }
        }
        return null; // No next level (at max level)
    }
    
    /**
     * Convert a UserLadderStatus entity to a LadderStatusDTO.
     * 
     * @param status The entity to convert.
     * @return The converted DTO.
     */
    private LadderStatusDTO convertToDTO(UserLadderStatus status) {
        return LadderStatusDTO.builder()
                .currentLevel(status.getCurrentLevel().getLevel())
                .levelLabel(status.getCurrentLevel().getLabel())
                .earnedPoints(status.getEarnedPoints())
                .pointsToNextLevel(status.getPointsToNextLevel())
                .build();
    }
}
