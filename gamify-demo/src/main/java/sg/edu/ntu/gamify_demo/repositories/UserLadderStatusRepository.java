package sg.edu.ntu.gamify_demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sg.edu.ntu.gamify_demo.models.LadderLevel;
import sg.edu.ntu.gamify_demo.models.UserLadderStatus;

/**
 * Repository for UserLadderStatus entities.
 * Provides CRUD operations and custom queries for user ladder statuses.
 */
@Repository
public interface UserLadderStatusRepository extends JpaRepository<UserLadderStatus, String> {
    
    /**
     * Check if any users are at a specific level.
     * 
     * @param level The level to check.
     * @return True if any users are at the level, false otherwise.
     */
    boolean existsByCurrentLevel(LadderLevel level);
}
