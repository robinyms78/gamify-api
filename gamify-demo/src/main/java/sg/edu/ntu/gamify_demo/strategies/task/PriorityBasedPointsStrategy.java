package sg.edu.ntu.gamify_demo.strategies.task;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Strategy implementation that calculates points based on task priority.
 * Different priorities are assigned different point values.
 */
@Component
public class PriorityBasedPointsStrategy implements TaskPointsCalculationStrategy {
    
    // Define points for different task priorities
    private static final Map<String, Integer> PRIORITY_POINTS = new HashMap<>();
    
    static {
        PRIORITY_POINTS.put("LOW", 10);
        PRIORITY_POINTS.put("MEDIUM", 20);
        PRIORITY_POINTS.put("HIGH", 30);
        PRIORITY_POINTS.put("CRITICAL", 50);
        // Default points if priority is not specified
        PRIORITY_POINTS.put("DEFAULT", 15);
    }
    
    @Override
    public int calculatePoints(String taskId, JsonNode eventData) {
        // Determine points based on task priority
        String priority = eventData.has("priority") ? 
                eventData.get("priority").asText().toUpperCase() : "DEFAULT";
        
        return PRIORITY_POINTS.getOrDefault(priority, PRIORITY_POINTS.get("DEFAULT"));
    }
}
