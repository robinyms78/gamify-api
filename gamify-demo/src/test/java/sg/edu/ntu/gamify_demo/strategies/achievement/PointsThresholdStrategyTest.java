package sg.edu.ntu.gamify_demo.strategies.achievement;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sg.edu.ntu.gamify_demo.models.User;

/**
 * Unit tests for the PointsThresholdStrategy class.
 */
@ExtendWith(MockitoExtension.class)
public class PointsThresholdStrategyTest {
    
    @InjectMocks
    private PointsThresholdStrategy strategy;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    public void testEvaluate_UserMeetsThreshold_ReturnsTrue() {
        // Arrange
        User user = new User();
        user.setEarnedPoints(100L);
        
        ObjectNode criteria = objectMapper.createObjectNode();
        criteria.put("type", "POINTS_THRESHOLD");
        criteria.put("threshold", 50);
        
        // Act
        boolean result = strategy.evaluate(user, criteria);
        
        // Assert
        assertTrue(result, "User with 100 points should meet the 50 point threshold");
    }
    
    @Test
    public void testEvaluate_UserBelowThreshold_ReturnsFalse() {
        // Arrange
        User user = new User();
        user.setEarnedPoints(30L);
        
        ObjectNode criteria = objectMapper.createObjectNode();
        criteria.put("type", "POINTS_THRESHOLD");
        criteria.put("threshold", 50);
        
        // Act
        boolean result = strategy.evaluate(user, criteria);
        
        // Assert
        assertFalse(result, "User with 30 points should not meet the 50 point threshold");
    }
    
    @Test
    public void testEvaluate_UserEqualsThreshold_ReturnsTrue() {
        // Arrange
        User user = new User();
        user.setEarnedPoints(50L);
        
        ObjectNode criteria = objectMapper.createObjectNode();
        criteria.put("type", "POINTS_THRESHOLD");
        criteria.put("threshold", 50);
        
        // Act
        boolean result = strategy.evaluate(user, criteria);
        
        // Assert
        assertTrue(result, "User with 50 points should meet the 50 point threshold");
    }
    
    @Test
    public void testEvaluate_NullCriteria_ReturnsFalse() {
        // Arrange
        User user = new User();
        user.setEarnedPoints(100L);
        
        // Act
        boolean result = strategy.evaluate(user, null);
        
        // Assert
        assertFalse(result, "Null criteria should return false");
    }
    
    @Test
    public void testEvaluate_MissingThreshold_ReturnsFalse() {
        // Arrange
        User user = new User();
        user.setEarnedPoints(100L);
        
        ObjectNode criteria = objectMapper.createObjectNode();
        criteria.put("type", "POINTS_THRESHOLD");
        // No threshold specified
        
        // Act
        boolean result = strategy.evaluate(user, criteria);
        
        // Assert
        assertFalse(result, "Criteria without threshold should return false");
    }
}
