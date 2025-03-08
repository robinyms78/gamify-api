package sg.edu.ntu.gamify_demo.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sg.edu.ntu.gamify_demo.events.EventPublisher;
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.models.PointsTransaction;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.repositories.PointsTransactionRepository;

/**
 * Service for handling points-related operations.
 * This service is responsible for awarding, spending, and tracking points.
 */
@Service
public class PointsService {
    
    private final UserService userService;
    private final PointsTransactionRepository pointsTransactionRepository;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final LadderService ladderService;
    
    /**
     * Constructor for dependency injection.
     */
    @Autowired
    public PointsService(
            UserService userService,
            PointsTransactionRepository pointsTransactionRepository,
            EventPublisher eventPublisher,
            ObjectMapper objectMapper,
            LadderService ladderService) {
        this.userService = userService;
        this.pointsTransactionRepository = pointsTransactionRepository;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.ladderService = ladderService;
    }
    
    /**
     * Get a user's earned points.
     * 
     * @param userId The ID of the user.
     * @return The user's earned points.
     */
    public int getUserPoints(String userId) {
        User user = userService.getUserById(userId);
        return user != null ? user.getEarnedPoints() : 0;
    }
    
    /**
     * Award points to a user and record the transaction.
     * 
     * @param userId The ID of the user.
     * @param points The number of points to award.
     * @param source The source of the points (e.g., "TASK_COMPLETED").
     * @param metadata Additional data about the transaction.
     * @return The user's new total earned points.
     */
    @Transactional
    public int awardPoints(String userId, int points, String source, JsonNode metadata) {
        User user = userService.getUserById(userId);
        
        if (user == null) {
            return 0;
        }
        
        // Update user's points
        int currentPoints = user.getEarnedPoints();
        int newPoints = currentPoints + points;
        user.setEarnedPoints(newPoints);
        user.setAvailablePoints(user.getAvailablePoints() + points);
        userService.updateUser(user.getId(), user);
        
        // Create a points transaction
        PointsTransaction transaction = new PointsTransaction(user, source, points, metadata);
        transaction.setCreatedAt(LocalDateTime.now());
        pointsTransactionRepository.save(transaction);
        
        // Update the user's ladder status
        ladderService.updateUserLadderStatus(user.getId());
        
        // Publish points earned event
        if (eventPublisher != null) {
            ObjectNode eventData = objectMapper.createObjectNode();
            eventData.put("points", points);
            eventData.put("newTotal", newPoints);
            eventData.put("source", source);
            
            if (metadata != null) {
                eventData.set("metadata", metadata);
            }
            
            eventPublisher.publishEvent("POINTS_EARNED", user, eventData);
        }
        
        return newPoints;
    }
    
    /**
     * Spend points from a user's available points.
     * 
     * @param userId The ID of the user.
     * @param points The number of points to spend.
     * @param source The source of the spend (e.g., "REWARD_REDEMPTION").
     * @param metadata Additional data about the transaction.
     * @return True if the points were successfully spent, false if the user doesn't have enough points.
     */
    @Transactional
    public boolean spendPoints(String userId, int points, String source, JsonNode metadata) {
        User user = userService.getUserById(userId);
        
        if (user == null) {
            return false;
        }
        
        int availablePoints = user.getAvailablePoints();
        
        if (availablePoints < points) {
            return false;
        }
        
        // Update user's available points
        user.setAvailablePoints(availablePoints - points);
        userService.updateUser(user.getId(), user);
        
        // Create a points transaction (negative points for spending)
        PointsTransaction transaction = new PointsTransaction(user, source, -points, metadata);
        transaction.setCreatedAt(LocalDateTime.now());
        pointsTransactionRepository.save(transaction);
        
        // Publish points spent event
        if (eventPublisher != null) {
            ObjectNode eventData = objectMapper.createObjectNode();
            eventData.put("points", points);
            eventData.put("newTotal", user.getAvailablePoints());
            eventData.put("source", source);
            
            if (metadata != null) {
                eventData.set("metadata", metadata);
            }
            
            eventPublisher.publishEvent("POINTS_SPENT", user, eventData);
        }
        
        return true;
    }
}
