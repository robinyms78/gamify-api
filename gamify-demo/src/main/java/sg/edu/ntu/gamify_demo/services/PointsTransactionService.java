package sg.edu.ntu.gamify_demo.services;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import sg.edu.ntu.gamify_demo.models.PointsTransaction;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.repositories.PointsTransactionRepository;
import sg.edu.ntu.gamify_demo.repositories.UserRepository;

/**
 * Service for managing points transactions.
 * This service centralizes the logic for recording points earned and spent,
 * ensuring consistent updates to user point balances.
 */
@Service
@RequiredArgsConstructor
public class PointsTransactionService {
    
    private final PointsTransactionRepository pointsTransactionRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final MessageBrokerService messageBroker;
    
    /**
     * Records points earned by a user and updates their point balances.
     * 
     * @param user The user earning points
     * @param points The number of points earned (must be positive)
     * @param eventType The type of event that triggered the points earning
     * @param metadata Additional data about the event
     * @return The created points transaction
     * @throws IllegalArgumentException if points is not positive
     */
    @Transactional
    public PointsTransaction recordPointsEarned(User user, long points, String eventType, JsonNode metadata) {
        if (points <= 0) {
            throw new IllegalArgumentException("Points earned must be positive");
        }
        
        // Create transaction
        PointsTransaction transaction = new PointsTransaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setUser(user);
        transaction.setEventType(eventType);
        transaction.setPoints(points);
        transaction.setTimestamp(ZonedDateTime.now());
        transaction.setMetadata(metadata);
        
        // Update user points
        user.setEarnedPoints(user.getEarnedPoints() + points);
        user.setAvailablePoints(user.getAvailablePoints() + points);
        userRepository.save(user);
        
        // Save transaction
        PointsTransaction savedTransaction = pointsTransactionRepository.save(transaction);
        
        // Send notification
        sendPointsNotification(user, points, "POINTS_EARNED", user.getAvailablePoints());
        
        return savedTransaction;
    }
    
    /**
     * Records points spent by a user and updates their available points balance.
     * 
     * @param user The user spending points
     * @param points The number of points to spend (must be positive)
     * @param eventType The type of event that triggered the points spending
     * @param metadata Additional data about the event
     * @return The created points transaction, or null if the user doesn't have enough points
     * @throws IllegalArgumentException if points is not positive
     */
    @Transactional
    public PointsTransaction recordPointsSpent(User user, long points, String eventType, JsonNode metadata) {
        if (points <= 0) {
            throw new IllegalArgumentException("Points spent must be positive");
        }
        
        // Check if user has enough points
        if (user.getAvailablePoints() < points) {
            return null;
        }
        
        // Create transaction (with negative points to indicate spending)
        PointsTransaction transaction = new PointsTransaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setUser(user);
        transaction.setEventType(eventType);
        transaction.setPoints(-points); // Negative for spending
        transaction.setTimestamp(ZonedDateTime.now());
        transaction.setMetadata(metadata);
        
        // Update user points (only available points, earned points remain unchanged)
        user.setAvailablePoints(user.getAvailablePoints() - points);
        userRepository.save(user);
        
        // Save transaction
        PointsTransaction savedTransaction = pointsTransactionRepository.save(transaction);
        
        // Send notification
        sendPointsNotification(user, points, "POINTS_SPENT", user.getAvailablePoints());
        
        return savedTransaction;
    }
    
    /**
     * Gets all points transactions for a user.
     * 
     * @param user The user to get transactions for
     * @return A list of points transactions
     */
    public List<PointsTransaction> getTransactionsForUser(User user) {
        return pointsTransactionRepository.findByUser(user);
    }
    
    /**
     * Gets all points transactions of a specific event type.
     * 
     * @param eventType The event type to filter by
     * @return A list of points transactions
     */
    public List<PointsTransaction> getTransactionsByEventType(String eventType) {
        return pointsTransactionRepository.findByEventType(eventType);
    }
    
    /**
     * Gets all points transactions for a user and event type.
     * 
     * @param user The user to get transactions for
     * @param eventType The event type to filter by
     * @return A list of points transactions
     */
    public List<PointsTransaction> getTransactionsForUserAndEventType(User user, String eventType) {
        return pointsTransactionRepository.findByUserAndEventType(user, eventType);
    }
    
    /**
     * Helper method to send a notification about a points transaction.
     */
    private void sendPointsNotification(User user, long points, String eventType, long newBalance) {
        if (messageBroker != null) {
            ObjectNode notification = objectMapper.createObjectNode();
            notification.put("userId", user.getId());
            notification.put("eventType", eventType);
            notification.put("points", points);
            notification.put("newBalance", newBalance);
            
            messageBroker.sendNotification("points", notification);
        }
    }
}
