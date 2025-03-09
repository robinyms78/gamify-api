package sg.edu.ntu.gamify_demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sg.edu.ntu.gamify_demo.models.PointsTransaction;
import sg.edu.ntu.gamify_demo.models.User;

import java.util.List;

/**
 * Repository for PointsTransaction entities.
 */
@Repository
public interface PointsTransactionRepository extends JpaRepository<PointsTransaction, String> {
    
    /**
     * Find all points transactions for a specific user.
     * 
     * @param user The user to find transactions for.
     * @return A list of points transactions for the user.
     */
    List<PointsTransaction> findByUser(User user);
    
    /**
     * Find all points transactions of a specific event type.
     * 
     * @param eventType The type of event.
     * @return A list of points transactions of the specified event type.
     */
    List<PointsTransaction> findByEventType(String eventType);
    
    /**
     * Find all points transactions for a specific user and event type.
     * 
     * @param user The user to find transactions for.
     * @param eventType The type of event.
     * @return A list of points transactions for the user and event type.
     */
    List<PointsTransaction> findByUserAndEventType(User user, String eventType);
}
