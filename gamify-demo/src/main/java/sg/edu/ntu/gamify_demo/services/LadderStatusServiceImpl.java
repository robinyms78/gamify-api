package sg.edu.ntu.gamify_demo.services;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import sg.edu.ntu.gamify_demo.dtos.LadderStatusDTO;
import sg.edu.ntu.gamify_demo.interfaces.LadderStatusService;
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.mappers.LadderStatusMapper;
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
        Long pointsToNextLevel = pointsCalculationStrategy.calculatePointsToNextLevel(
                user.getEarnedPoints(), newLevel, nextLevel);
        
        status.setPointsToNextLevel(pointsToNextLevel);
        status.setUpdatedAt(ZonedDateTime.now());
        
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
    @Transactional(propagation = Propagation.REQUIRED)
    private UserLadderStatus initializeUserLadderStatus(User user) {
        try {
            // Add transaction boundary logging
            System.out.println("LadderStatusServiceImpl: Starting transaction for user ladder status initialization: " + 
                (user != null ? user.getId() : "null user"));
            
            // Ensure user is not null and has a valid ID
            if (user == null || user.getId() == null) {
                throw new IllegalArgumentException("User or user ID is null");
            }
            
            // Get the first ladder level
            LadderLevel firstLevel = ladderLevelRepository.findByLevel(1L);
            
            if (firstLevel == null) {
                // Create a default first level if none exists
                System.out.println("LadderStatusServiceImpl: First ladder level not found, creating default level");
                firstLevel = new LadderLevel();
                firstLevel.setLevel(1L);
                firstLevel.setLabel("Beginner");
                firstLevel.setPointsRequired(0L);
                firstLevel.setCreatedAt(ZonedDateTime.now());
                
                try {
                    firstLevel = ladderLevelRepository.save(firstLevel);
                    System.out.println("LadderStatusServiceImpl: Created default ladder level: " + firstLevel.getLevel());
                    
                    // Verify the level was saved correctly
                    LadderLevel verifyLevel = ladderLevelRepository.findByLevel(1L);
                    if (verifyLevel == null) {
                        System.err.println("LadderStatusServiceImpl: ERROR: Failed to save default ladder level!");
                    } else {
                        System.out.println("LadderStatusServiceImpl: Verified default ladder level exists with ID: " + verifyLevel.getLevel());
                    }
                } catch (Exception e) {
                    System.err.println("LadderStatusServiceImpl: Error saving default ladder level: " + e.getMessage());
                    e.printStackTrace();
                    
                    // Try one more time with a direct constructor
                    firstLevel = new LadderLevel(1L, "Beginner", 0L);
                    firstLevel = ladderLevelRepository.save(firstLevel);
                }
            } else {
                System.out.println("LadderStatusServiceImpl: Found existing ladder level: " + firstLevel.getLevel());
            }
            
            // Create a new ladder status for the user with explicit ID assignment
            UserLadderStatus status = new UserLadderStatus();
            status.setId(user.getId()); // Explicit ID assignment
            status.setUser(user);
            status.setCurrentLevel(firstLevel);
            status.setEarnedPoints(user.getEarnedPoints() != null ? user.getEarnedPoints() : 0L);
            status.setPointsToNextLevel(100L); // Default points to next level
            
            // Find the next level
            List<LadderLevel> levels = ladderLevelRepository.findAllByOrderByLevelAsc();
            LadderLevel nextLevel = findNextLevel(levels, firstLevel);
            
            // Calculate points to next level using the strategy
            if (nextLevel != null) {
                Long pointsToNextLevel = pointsCalculationStrategy.calculatePointsToNextLevel(
                        user.getEarnedPoints() != null ? user.getEarnedPoints() : 0L, 
                        firstLevel, 
                        nextLevel);
                status.setPointsToNextLevel(pointsToNextLevel);
            }
            
            status.setUpdatedAt(ZonedDateTime.now());
            
            // Double-check ID is set before saving
            if (status.getId() == null) {
                System.out.println("LadderStatusServiceImpl: WARNING: ID is still null after explicit assignment, setting from user ID");
                status.setId(user.getId());
            }
            
            System.out.println("LadderStatusServiceImpl: Saving UserLadderStatus with ID: " + status.getId());
            UserLadderStatus savedStatus = userLadderStatusRepository.save(status);
            System.out.println("LadderStatusServiceImpl: Successfully saved UserLadderStatus with ID: " + savedStatus.getId());
            
            return savedStatus;
        } catch (Exception e) {
            // Log the exception for debugging
            System.err.println("Error initializing UserLadderStatus: " + e.getMessage());
            e.printStackTrace();
            
            // Create a minimal valid status to avoid constraint violations
            if (user == null || user.getId() == null) {
                throw new IllegalArgumentException("User or user ID is null");
            }
            
            // Get or create a default level
            LadderLevel defaultLevel = ensureDefaultLevelExists();
            
            // Create using constructor to ensure ID is set
            UserLadderStatus status = new UserLadderStatus(
                user,
                defaultLevel,
                0L,
                100L
            );
            
            status.setUpdatedAt(ZonedDateTime.now());
            
            // Double-check ID is set before saving
            if (status.getId() == null) {
                status.setId(user.getId());
            }
            
            return userLadderStatusRepository.save(status);
        }
    }
    
    /**
     * Ensures that a default ladder level exists and returns it.
     * 
     * @return The default ladder level.
     */
    private LadderLevel ensureDefaultLevelExists() {
        LadderLevel defaultLevel = ladderLevelRepository.findByLevel(1L);
        if (defaultLevel == null) {
            defaultLevel = new LadderLevel();
            defaultLevel.setLevel(1L);
            defaultLevel.setLabel("Beginner");
            defaultLevel.setPointsRequired(0L);
            defaultLevel.setCreatedAt(ZonedDateTime.now());
            defaultLevel = ladderLevelRepository.save(defaultLevel);
        }
        return defaultLevel;
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
        return LadderStatusMapper.INSTANCE.toDTO(status);
    }
}
