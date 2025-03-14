package sg.edu.ntu.gamify_demo.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import sg.edu.ntu.gamify_demo.models.Leaderboard;
import sg.edu.ntu.gamify_demo.models.User;

/**
 * Repository interface for Leaderboard entity.
 * Provides methods to interact with the leaderboard table in the database.
 */
@Repository
public interface LeaderboardRepository extends JpaRepository<Leaderboard, String> {
    
    /**
     * Finds a leaderboard entry by user.
     * 
     * @param user The user whose leaderboard entry to find.
     * @return An Optional containing the leaderboard entry if found, or empty if not found.
     */
    Optional<Leaderboard> findByUser(User user);
    
    /**
     * Finds all leaderboard entries ordered by rank.
     * 
     * @return A list of leaderboard entries ordered by rank.
     */
    List<Leaderboard> findAllByOrderByRankAsc();
    
    /**
     * Finds all leaderboard entries for a specific department ordered by rank.
     * 
     * @param department The department to filter by.
     * @return A list of leaderboard entries for the specified department ordered by rank.
     */
    List<Leaderboard> findByDepartmentOrderByRankAsc(String department);
    
    /**
     * Finds the top N users on the leaderboard.
     * 
     * @param limit The number of top users to retrieve.
     * @return A list of the top N leaderboard entries.
     */
    @Query(value = "SELECT l FROM Leaderboard l ORDER BY l.rank ASC LIMIT ?1")
    List<Leaderboard> findTopUsers(int limit);
}
