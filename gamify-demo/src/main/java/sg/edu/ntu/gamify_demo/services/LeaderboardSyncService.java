package sg.edu.ntu.gamify_demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sg.edu.ntu.gamify_demo.models.Leaderboard;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.repositories.LeaderboardRepository;
import sg.edu.ntu.gamify_demo.repositories.UserRepository;

/**
 * Service responsible for synchronizing Leaderboard data with User data.
 * This ensures that whenever User data changes, the Leaderboard is updated accordingly.
 */
@Service
public class LeaderboardSyncService {

    private final LeaderboardRepository leaderboardRepository;
    private final UserRepository userRepository;

    @Autowired
    public LeaderboardSyncService(LeaderboardRepository leaderboardRepository, UserRepository userRepository) {
        this.leaderboardRepository = leaderboardRepository;
        this.userRepository = userRepository;
    }

    /**
     * Synchronizes a user's leaderboard entry with their current data.
     * 
     * @param userId The ID of the user whose leaderboard entry should be synchronized.
     * @return The updated Leaderboard entry, or null if the user doesn't exist.
     */
    @Transactional
    public Leaderboard syncUserLeaderboard(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }

        Leaderboard leaderboard = leaderboardRepository.findByUser(user).orElse(null);
        if (leaderboard == null) {
            // If the user doesn't have a leaderboard entry yet, we can't sync
            return null;
        }

        // Sync the leaderboard data with the user data
        leaderboard.syncWithUser();
        return leaderboardRepository.save(leaderboard);
    }

    /**
     * Synchronizes all leaderboard entries with their respective users' data.
     * This is useful for bulk updates or scheduled synchronization.
     */
    @Transactional
    public void syncAllLeaderboards() {
        leaderboardRepository.findAll().forEach(leaderboard -> {
            leaderboard.syncWithUser();
            leaderboardRepository.save(leaderboard);
        });
    }

    /**
     * Updates the rank of a user on the leaderboard.
     * 
     * @param userId The ID of the user whose rank should be updated.
     * @param newRank The new rank for the user.
     * @return The updated Leaderboard entry, or null if the user doesn't exist.
     */
    @Transactional
    public Leaderboard updateUserRank(String userId, int newRank) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }

        Leaderboard leaderboard = leaderboardRepository.findByUser(user).orElse(null);
        if (leaderboard == null) {
            return null;
        }

        leaderboard.updateRank(newRank);
        return leaderboardRepository.save(leaderboard);
    }
}
