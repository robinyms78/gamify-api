package sg.edu.ntu.gamify_demo.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sg.edu.ntu.gamify_demo.interfaces.LeaderboardService;
import sg.edu.ntu.gamify_demo.models.LadderLevel;
import sg.edu.ntu.gamify_demo.models.Leaderboard;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.repositories.LadderLevelRepository;
import sg.edu.ntu.gamify_demo.repositories.LeaderboardRepository;
import sg.edu.ntu.gamify_demo.repositories.UserRepository;

/**
 * Implementation of the LeaderboardService interface.
 * Provides methods for managing leaderboard entries and rankings.
 */
@Service
public class LeaderboardServiceImpl implements LeaderboardService {

    private final LeaderboardRepository leaderboardRepository;
    private final UserRepository userRepository;
    private final LadderLevelRepository ladderLevelRepository;
    
    @Autowired
    public LeaderboardServiceImpl(
            LeaderboardRepository leaderboardRepository,
            UserRepository userRepository,
            LadderLevelRepository ladderLevelRepository) {
        this.leaderboardRepository = leaderboardRepository;
        this.userRepository = userRepository;
        this.ladderLevelRepository = ladderLevelRepository;
    }
    
    @Override
    @Transactional
    public int calculateRanks() {
        // Get all leaderboard entries
        List<Leaderboard> leaderboards = leaderboardRepository.findAll();
        
        // Sort by earned points in descending order
        leaderboards.sort(Comparator.comparing(Leaderboard::getEarnedPoints).reversed());
        
        // Group by points to handle ties
        Map<Long, List<Leaderboard>> pointsGroups = leaderboards.stream()
                .collect(Collectors.groupingBy(Leaderboard::getEarnedPoints));
        
        // Assign ranks
        long currentRank = 1;
        int updatedCount = 0;
        
        List<Long> sortedPoints = pointsGroups.keySet().stream()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        
        for (Long points : sortedPoints) {
            List<Leaderboard> group = pointsGroups.get(points);
            
            // All users with the same points get the same rank
            for (Leaderboard leaderboard : group) {
                if (leaderboard.getRank() == null || leaderboard.getRank() != currentRank) {
                    leaderboard.setRank(currentRank);
                    leaderboardRepository.save(leaderboard);
                    updatedCount++;
                }
            }
            
            // Increment rank by the size of the group for the next point value
            currentRank += group.size();
        }
        
        // Explicitly flush changes to ensure they're persisted
        leaderboardRepository.flush();
        
        return updatedCount;
    }

    @Override
    public Page<Leaderboard> getGlobalRankings(Pageable pageable) {
        // Get all entries ordered by rank
        List<Leaderboard> allLeaderboards = leaderboardRepository.findAllByOrderByRankAsc();
        
        // Calculate total elements
        int total = allLeaderboards.size();
        
        // Calculate start and end indices for the requested page
        int start = (int) Math.min(pageable.getOffset(), total);
        int end = Math.min(start + pageable.getPageSize(), total);
        
        // Extract the sublist for the current page
        List<Leaderboard> pageContent = start < end ? allLeaderboards.subList(start, end) : new ArrayList<>();
        
        // Create and return a Page object
        return new PageImpl<>(pageContent, pageable, total);
    }

    @Override
    public Page<Leaderboard> getDepartmentRankings(String department, Pageable pageable) {
        // Get all entries for the department ordered by rank
        List<Leaderboard> departmentLeaderboards = leaderboardRepository.findByDepartmentOrderByRankAsc(department);
        
        // Calculate total elements
        int total = departmentLeaderboards.size();
        
        // Calculate start and end indices for the requested page
        int start = (int) Math.min(pageable.getOffset(), total);
        int end = Math.min(start + pageable.getPageSize(), total);
        
        // Extract the sublist for the current page
        List<Leaderboard> pageContent = start < end ? departmentLeaderboards.subList(start, end) : new ArrayList<>();
        
        // Create and return a Page object
        return new PageImpl<>(pageContent, pageable, total);
    }

    @Override
    public Leaderboard getUserRank(String userId) {
        return leaderboardRepository.findById(userId).orElse(null);
    }

    @Override
    public List<Leaderboard> getTopUsers(int limit) {
        return leaderboardRepository.findTopUsers(limit);
    }

    @Override
    @Transactional
    public Leaderboard createLeaderboardEntry(User user) {
        // Check if the user already has a leaderboard entry
        if (leaderboardRepository.existsById(user.getId())) {
            return leaderboardRepository.findById(user.getId()).orElse(null);
        }
        
        // Get the first ladder level
        LadderLevel firstLevel = ladderLevelRepository.findByLevel(1L);
        if (firstLevel == null) {
            // Create a default first level if none exists
            firstLevel = new LadderLevel(1L, "Beginner", 0L);
            firstLevel = ladderLevelRepository.save(firstLevel);
        }
        
        // Calculate initial rank
        long initialRank = leaderboardRepository.count() + 1;
        
        // Create a new leaderboard entry using the constructor that sets all fields
        Leaderboard leaderboard = new Leaderboard(user, user.getEarnedPoints(), firstLevel, initialRank);
        
        // Save the leaderboard entry
        Leaderboard savedLeaderboard = leaderboardRepository.save(leaderboard);
        
        // Ensure bidirectional relationship is properly set
        user.setLeaderboardEntry(savedLeaderboard);
        userRepository.save(user);
        
        // Explicitly flush changes to ensure they're persisted
        leaderboardRepository.flush();
        userRepository.flush();
        
        return savedLeaderboard;
    }

    @Override
    @Transactional
    public Leaderboard updateLeaderboardEntry(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }
        
        Leaderboard leaderboard = leaderboardRepository.findById(userId).orElse(null);
        if (leaderboard == null) {
            // If the user doesn't have a leaderboard entry yet, create one
            return createLeaderboardEntry(user);
        }
        
        // Update the leaderboard entry with the latest user data
        leaderboard.setUsername(user.getUsername());
        leaderboard.setDepartment(user.getDepartment());
        leaderboard.setEarnedPoints(user.getEarnedPoints());
        
        // Get the appropriate ladder level based on earned points
        List<LadderLevel> levels = ladderLevelRepository.findAllByOrderByLevelAsc();
        // Sort by points required in ascending order
        levels.sort(Comparator.comparing(LadderLevel::getPointsRequired));
        LadderLevel appropriateLevel = levels.get(0); // Default to the first level
        
        for (LadderLevel level : levels) {
            if (user.getEarnedPoints() >= level.getPointsRequired()) {
                appropriateLevel = level;
            } else {
                break;
            }
        }
        
        // Update the level if needed
        if (leaderboard.getCurrentLevel() == null || !leaderboard.getCurrentLevel().equals(appropriateLevel)) {
            leaderboard.setCurrentLevel(appropriateLevel);
        }
        
        // Save the updated leaderboard entry
        Leaderboard savedLeaderboard = leaderboardRepository.save(leaderboard);
        
        // Ensure bidirectional relationship is properly set
        user.setLeaderboardEntry(savedLeaderboard);
        userRepository.save(user);
        
        // Explicitly flush changes to ensure they're persisted
        leaderboardRepository.flush();
        userRepository.flush();
        
        return savedLeaderboard;
    }
}
