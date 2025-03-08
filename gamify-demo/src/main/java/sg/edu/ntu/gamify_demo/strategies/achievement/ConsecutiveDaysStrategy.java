package sg.edu.ntu.gamify_demo.strategies.achievement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import sg.edu.ntu.gamify_demo.models.PointsTransaction;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.repositories.PointsTransactionRepository;

/**
 * Strategy implementation for evaluating consecutive days achievement criteria.
 * This strategy checks if a user has been active for a consecutive number of days.
 */
@Component
public class ConsecutiveDaysStrategy implements AchievementCriteriaStrategy {

    private final PointsTransactionRepository pointsTransactionRepository;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param pointsTransactionRepository Repository for points transactions.
     */
    @Autowired
    public ConsecutiveDaysStrategy(PointsTransactionRepository pointsTransactionRepository) {
        this.pointsTransactionRepository = pointsTransactionRepository;
    }
    
    /**
     * Evaluates whether a user has been active for a consecutive number of days.
     * 
     * @param user The user to evaluate.
     * @param criteria The criteria containing the required consecutive days.
     * @return true if the user has been active for the required consecutive days, false otherwise.
     */
    @Override
    public boolean evaluate(User user, JsonNode criteria) {
        if (criteria == null || !criteria.has("days")) {
            return false;
        }
        
        int requiredDays = criteria.get("days").asInt();
        
        // Get all points transactions for the user
        List<PointsTransaction> transactions = pointsTransactionRepository.findByUser(user);
        
        // Extract unique dates from transaction timestamps
        Set<LocalDate> activityDates = transactions.stream()
                .map(PointsTransaction::getTimestamp)
                .filter(timestamp -> timestamp != null)
                .map(LocalDateTime::toLocalDate)
                .collect(Collectors.toSet());
        
        // Check for consecutive days
        return hasConsecutiveDays(activityDates, requiredDays);
    }
    
    /**
     * Checks if a set of dates contains a sequence of consecutive days of the required length.
     * 
     * @param dates Set of activity dates.
     * @param requiredDays Number of consecutive days required.
     * @return true if the set contains a sequence of consecutive days of the required length, false otherwise.
     */
    private boolean hasConsecutiveDays(Set<LocalDate> dates, int requiredDays) {
        if (dates.size() < requiredDays) {
            return false;
        }
        
        // Sort dates
        List<LocalDate> sortedDates = dates.stream()
                .sorted()
                .collect(Collectors.toList());
        
        int maxConsecutive = 1;
        int currentConsecutive = 1;
        
        for (int i = 1; i < sortedDates.size(); i++) {
            // Check if current date is one day after previous date
            if (sortedDates.get(i).minusDays(1).equals(sortedDates.get(i - 1))) {
                currentConsecutive++;
                maxConsecutive = Math.max(maxConsecutive, currentConsecutive);
                
                if (maxConsecutive >= requiredDays) {
                    return true;
                }
            } else if (!sortedDates.get(i).equals(sortedDates.get(i - 1))) {
                // Reset counter if dates are not consecutive (and not the same date)
                currentConsecutive = 1;
            }
        }
        
        return maxConsecutive >= requiredDays;
    }
}
