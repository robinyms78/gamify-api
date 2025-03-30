package sg.edu.ntu.gamify_demo.interfaces;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import sg.edu.ntu.gamify_demo.models.Leaderboard;
import sg.edu.ntu.gamify_demo.models.User;

/**
 * Service interface for leaderboard operations.
 * Provides methods for retrieving, creating, and updating leaderboard entries.
 */
public interface LeaderboardService {
    
    /**
     * Calculates ranks for all users based on their earned points.
     * Users with the same number of points will have the same rank.
     * 
     * @return The number of leaderboard entries updated.
     */
    int calculateRanks();
    
    /**
     * Retrieves global rankings for all users.
     * 
     * @param pageable Pagination information.
     * @return A page of leaderboard entries ordered by rank.
     */
    Page<Leaderboard> getGlobalRankings(Pageable pageable);
    
    /**
     * Retrieves rankings for users in a specific department.
     * 
     * @param department The department to filter by.
     * @param pageable Pagination information.
     * @return A page of leaderboard entries for the specified department ordered by rank.
     */
    Page<Leaderboard> getDepartmentRankings(String department, Pageable pageable);
    
    /**
     * Retrieves a user's rank information.
     * 
     * @param userId The ID of the user.
     * @return The user's leaderboard entry, or null if not found.
     */
    Leaderboard getUserRank(String userId);
    
    /**
     * Retrieves the top N users on the leaderboard.
     * 
     * @param limit The number of top users to retrieve.
     * @return A list of the top N leaderboard entries.
     */
    List<Leaderboard> getTopUsers(int limit);
    
    /**
     * Creates a new leaderboard entry for a user.
     * 
     * @param user The user to create an entry for.
     * @return The created leaderboard entry.
     */
    Leaderboard createLeaderboardEntry(User user);
    
    /**
     * Updates a user's leaderboard entry.
     * 
     * @param userId The ID of the user whose entry to update.
     * @return The updated leaderboard entry, or null if the user doesn't exist.
     */
    Leaderboard updateLeaderboardEntry(String userId);
}
