package sg.edu.ntu.gamify_demo.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sg.edu.ntu.gamify_demo.models.LadderLevel;

/**
 * Repository for LadderLevel entities.
 * Provides CRUD operations and custom queries for ladder levels.
 */
@Repository
public interface LadderLevelRepository extends JpaRepository<LadderLevel, Long> {
    
    /**
     * Find a ladder level by its level number.
     * 
     * @param level The level number.
     * @return The LadderLevel if found, null otherwise.
     */
    LadderLevel findByLevel(int level);
    
    /**
     * Find all ladder levels ordered by level in ascending order.
     * 
     * @return A list of ladder levels.
     */
    List<LadderLevel> findAllByOrderByLevelAsc();
}
