package sg.edu.ntu.gamify_demo.repositories;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import sg.edu.ntu.gamify_demo.models.LadderLevel;
import sg.edu.ntu.gamify_demo.models.Leaderboard;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.enums.UserRole;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class LeaderboardRepositoryIntegrationTest {

    @Autowired
    private LeaderboardRepository leaderboardRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LadderLevelRepository ladderLevelRepository;
    
    private User user1;
    private User user2;
    private User user3;
    private LadderLevel level1;
    private LadderLevel level2;
    private Leaderboard leaderboard1;
    private Leaderboard leaderboard2;
    private Leaderboard leaderboard3;
    
    @BeforeEach
    void setUp() {
        // Clear existing data
        leaderboardRepository.deleteAll();
        userRepository.deleteAll();
        ladderLevelRepository.deleteAll();
        
        // Create ladder levels
        level1 = new LadderLevel(1L, "Beginner", 0L);
        level2 = new LadderLevel(2L, "Intermediate", 100L);
        
        ladderLevelRepository.save(level1);
        ladderLevelRepository.save(level2);
        
        // Create users
        user1 = User.builder()
                .id("user1")
                .username("user1")
                .email("user1@example.com")
                .passwordHash("hash1")
                .role(UserRole.EMPLOYEE)
                .department("Engineering")
                .earnedPoints(100L)
                .availablePoints(100L)
                .build();
        
        user2 = User.builder()
                .id("user2")
                .username("user2")
                .email("user2@example.com")
                .passwordHash("hash2")
                .role(UserRole.EMPLOYEE)
                .department("Engineering")
                .earnedPoints(200L)
                .availablePoints(200L)
                .build();
        
        user3 = User.builder()
                .id("user3")
                .username("user3")
                .email("user3@example.com")
                .passwordHash("hash3")
                .role(UserRole.EMPLOYEE)
                .department("Marketing")
                .earnedPoints(150L)
                .availablePoints(150L)
                .build();
        
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
        
        // Create leaderboard entries
        leaderboard1 = new Leaderboard(user1, 100L, level2, 3L);
        leaderboard2 = new Leaderboard(user2, 200L, level2, 1L);
        leaderboard3 = new Leaderboard(user3, 150L, level2, 2L);
        
        leaderboardRepository.save(leaderboard1);
        leaderboardRepository.save(leaderboard2);
        leaderboardRepository.save(leaderboard3);
    }
    
    @Test
    @DisplayName("Find by user should return correct leaderboard entry")
    void findByUser_ShouldReturnCorrectLeaderboardEntry() {
        // Act
        Optional<Leaderboard> result = leaderboardRepository.findByUser(user1);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(user1.getId(), result.get().getUser().getId());
        assertEquals(user1.getUsername(), result.get().getUser().getUsername());
        assertEquals(3L, result.get().getRank());
    }
    
    @Test
    @DisplayName("Find all by order by rank asc should return ordered entries")
    void findAllByOrderByRankAsc_ShouldReturnOrderedEntries() {
        // Act
        List<Leaderboard> result = leaderboardRepository.findAllByOrderByRankAsc();
        
        // Assert
        assertEquals(3, result.size());
        assertEquals(user2.getId(), result.get(0).getUser().getId()); // Rank 1
        assertEquals(user3.getId(), result.get(1).getUser().getId()); // Rank 2
        assertEquals(user1.getId(), result.get(2).getUser().getId()); // Rank 3
    }
    
    @Test
    @DisplayName("Find by department order by rank asc should filter by department")
    void findByDepartmentOrderByRankAsc_ShouldFilterByDepartment() {
        // Act
        List<Leaderboard> engineeringResult = leaderboardRepository.findByDepartmentOrderByRankAsc("Engineering");
        List<Leaderboard> marketingResult = leaderboardRepository.findByDepartmentOrderByRankAsc("Marketing");
        
        // Assert
        assertEquals(2, engineeringResult.size());
        assertEquals(user2.getId(), engineeringResult.get(0).getUser().getId()); // Rank 1
        assertEquals(user1.getId(), engineeringResult.get(1).getUser().getId()); // Rank 3
        
        assertEquals(1, marketingResult.size());
        assertEquals(user3.getId(), marketingResult.get(0).getUser().getId()); // Rank 2
    }
    
    @Test
    @DisplayName("Find top users should return limited number of users")
    void findTopUsers_ShouldReturnLimitedNumberOfUsers() {
        // Act
        List<Leaderboard> result = leaderboardRepository.findTopUsers(2);
        
        // Assert
        assertEquals(2, result.size());
        assertEquals(user2.getId(), result.get(0).getUser().getId()); // Rank 1
        assertEquals(user3.getId(), result.get(1).getUser().getId()); // Rank 2
    }
}
