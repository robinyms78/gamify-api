package sg.edu.ntu.gamify_demo.services;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import sg.edu.ntu.gamify_demo.interfaces.LeaderboardService;
import sg.edu.ntu.gamify_demo.models.LadderLevel;
import sg.edu.ntu.gamify_demo.models.Leaderboard;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.enums.UserRole;
import sg.edu.ntu.gamify_demo.repositories.LadderLevelRepository;
import sg.edu.ntu.gamify_demo.repositories.LeaderboardRepository;
import sg.edu.ntu.gamify_demo.repositories.UserRepository;

/**
 * Integration tests for the LeaderboardService implementation.
 * Tests the service with actual database interactions.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class LeaderboardServiceIntegrationTest {

    @Autowired
    private LeaderboardService leaderboardService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LadderLevelRepository ladderLevelRepository;
    
    @Autowired
    private LeaderboardRepository leaderboardRepository;
    
    private User user1;
    private User user2;
    private User user3;
    private User user4;
    private LadderLevel level1;
    private LadderLevel level2;
    private LadderLevel level3;
    
    @BeforeEach
    void setUp() {
        // Clear any existing data
        leaderboardRepository.deleteAll();
        userRepository.deleteAll();
        ladderLevelRepository.deleteAll();
        
        // Create ladder levels
        level1 = new LadderLevel(1L, "Beginner", 0L);
        level2 = new LadderLevel(2L, "Intermediate", 100L);
        level3 = new LadderLevel(3L, "Advanced", 200L);
        
        level1 = ladderLevelRepository.save(level1);
        level2 = ladderLevelRepository.save(level2);
        level3 = ladderLevelRepository.save(level3);
        
        // Create test users with UUID IDs
        user1 = User.builder()
                .username("user1")
                .email("user1@example.com")
                .passwordHash("hash1")
                .role(UserRole.EMPLOYEE)
                .department("Engineering")
                .earnedPoints(150L)
                .availablePoints(150L)
                .build();
        
        user2 = User.builder()
                .username("user2")
                .email("user2@example.com")
                .passwordHash("hash2")
                .role(UserRole.EMPLOYEE)
                .department("Engineering")
                .earnedPoints(250L)
                .availablePoints(250L)
                .build();
        
        user3 = User.builder()
                .username("user3")
                .email("user3@example.com")
                .passwordHash("hash3")
                .role(UserRole.EMPLOYEE)
                .department("Marketing")
                .earnedPoints(150L)
                .availablePoints(150L)
                .build();
        
        user4 = User.builder()
                .username("user4")
                .email("user4@example.com")
                .passwordHash("hash4")
                .role(UserRole.EMPLOYEE)
                .department("Marketing")
                .earnedPoints(50L)
                .availablePoints(50L)
                .build();
        
        // Save users - this will generate IDs via @PrePersist if not set
        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);
        user3 = userRepository.save(user3);
        user4 = userRepository.save(user4);
    }
    
    @Test
    @DisplayName("Create leaderboard entries for users")
    void testCreateLeaderboardEntry() {
        // Act
        Leaderboard entry1 = leaderboardService.createLeaderboardEntry(user1);
        
        // Refresh user from database to ensure bidirectional relationship is loaded
        user1 = userRepository.findById(user1.getId()).orElseThrow();
        
        // Assert
        assertNotNull(entry1);
        assertEquals(user1.getId(), entry1.getUserId());
        assertEquals(user1.getUsername(), entry1.getUsername());
        assertEquals(user1.getDepartment(), entry1.getDepartment());
        assertEquals(user1.getEarnedPoints(), entry1.getEarnedPoints());
        
        // Verify the entry was saved to the database
        assertTrue(leaderboardRepository.existsById(user1.getId()));
        
        // Verify the bidirectional relationship is set
        assertNotNull(user1.getLeaderboard());
        assertEquals(user1.getId(), user1.getLeaderboard().getUserId());
    }
    
    @Test
    @DisplayName("Create leaderboard entry should return existing entry if it exists")
    void testCreateLeaderboardEntry_ExistingEntry() {
        // Arrange - Create initial entry
        Leaderboard entry1 = leaderboardService.createLeaderboardEntry(user1);
        assertNotNull(entry1);
        
        // Act - Try to create it again
        Leaderboard entry1Again = leaderboardService.createLeaderboardEntry(user1);
        
        // Assert
        assertNotNull(entry1Again);
        assertEquals(entry1.getUserId(), entry1Again.getUserId());
        
        // Verify only one entry exists
        assertEquals(1, leaderboardRepository.findAll().stream()
                .filter(e -> e.getUserId().equals(user1.getId()))
                .count());
    }
    
    @Test
    @DisplayName("Calculate ranks should assign ranks based on points")
    void testCalculateRanks() {
        // Arrange - Create leaderboard entries for all users
        leaderboardService.createLeaderboardEntry(user1);
        leaderboardService.createLeaderboardEntry(user2);
        leaderboardService.createLeaderboardEntry(user3);
        leaderboardService.createLeaderboardEntry(user4);
        
        // Act - Calculate ranks
        int updatedCount = leaderboardService.calculateRanks();
        
        // Assert - Verify number of updates
        assertEquals(4, updatedCount);
        
        // Retrieve updated entries from database
        Leaderboard entry1 = leaderboardRepository.findById(user1.getId()).orElse(null);
        Leaderboard entry2 = leaderboardRepository.findById(user2.getId()).orElse(null);
        Leaderboard entry3 = leaderboardRepository.findById(user3.getId()).orElse(null);
        Leaderboard entry4 = leaderboardRepository.findById(user4.getId()).orElse(null);
        
        // Verify all entries exist
        assertNotNull(entry1);
        assertNotNull(entry2);
        assertNotNull(entry3);
        assertNotNull(entry4);
        
        // Verify ranks are assigned correctly based on points
        assertEquals(1L, entry2.getRank()); // user2 has 250 points (highest)
        assertEquals(2L, entry1.getRank()); // user1 has 150 points (tied with user3)
        assertEquals(2L, entry3.getRank()); // user3 has 150 points (tied with user1)
        assertEquals(4L, entry4.getRank()); // user4 has 50 points (lowest)
    }
    
    @Test
    @DisplayName("Get global rankings should return paginated results")
    void testGetGlobalRankings() {
        // Arrange - Create and rank leaderboard entries
        leaderboardService.createLeaderboardEntry(user1);
        leaderboardService.createLeaderboardEntry(user2);
        leaderboardService.createLeaderboardEntry(user3);
        leaderboardService.createLeaderboardEntry(user4);
        leaderboardService.calculateRanks();
        
        // First page (2 items)
        Pageable pageable = PageRequest.of(0, 2);
        
        // Act - Get first page
        Page<Leaderboard> result = leaderboardService.getGlobalRankings(pageable);
        
        // Assert - Verify pagination metadata
        assertEquals(2, result.getContent().size());
        assertEquals(4, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        
        // Verify first page content (should be ordered by rank)
        List<Leaderboard> content = result.getContent();
        assertEquals(user2.getId(), content.get(0).getUserId()); // Rank 1 (highest points)
        
        // Get second page
        pageable = PageRequest.of(1, 2);
        result = leaderboardService.getGlobalRankings(pageable);
        
        // Verify second page
        assertEquals(2, result.getContent().size());
        content = result.getContent();
        assertEquals(user4.getId(), content.get(1).getUserId()); // Rank 4 (lowest points)
    }
    
    @Test
    @DisplayName("Get department rankings should filter by department")
    void testGetDepartmentRankings() {
        // Arrange - Create and rank leaderboard entries
        leaderboardService.createLeaderboardEntry(user1);
        leaderboardService.createLeaderboardEntry(user2);
        leaderboardService.createLeaderboardEntry(user3);
        leaderboardService.createLeaderboardEntry(user4);
        leaderboardService.calculateRanks();
        
        Pageable pageable = PageRequest.of(0, 10);
        
        // Act - Get rankings for each department
        Page<Leaderboard> engineeringResult = leaderboardService.getDepartmentRankings("Engineering", pageable);
        Page<Leaderboard> marketingResult = leaderboardService.getDepartmentRankings("Marketing", pageable);
        
        // Assert - Verify correct number of entries per department
        assertEquals(2, engineeringResult.getTotalElements());
        assertEquals(2, marketingResult.getTotalElements());
        
        // Verify Engineering department rankings
        List<Leaderboard> engineeringContent = engineeringResult.getContent();
        assertEquals(user2.getId(), engineeringContent.get(0).getUserId()); // Highest points in Engineering
        assertEquals(user1.getId(), engineeringContent.get(1).getUserId()); // Second highest in Engineering
        
        // Verify Marketing department rankings
        List<Leaderboard> marketingContent = marketingResult.getContent();
        assertEquals(user3.getId(), marketingContent.get(0).getUserId()); // Highest points in Marketing
        assertEquals(user4.getId(), marketingContent.get(1).getUserId()); // Second highest in Marketing
    }
    
    @Test
    @DisplayName("Get user rank should return user's leaderboard entry")
    void testGetUserRank() {
        // Arrange - Create leaderboard entry and calculate rank
        Leaderboard created = leaderboardService.createLeaderboardEntry(user1);
        leaderboardService.calculateRanks();
        
        // Act - Get user's rank
        Leaderboard result = leaderboardService.getUserRank(user1.getId());
        
        // Assert - Verify correct entry is returned
        assertNotNull(result);
        assertEquals(user1.getId(), result.getUserId());
        assertEquals(user1.getUsername(), result.getUsername());
        assertEquals(1L, result.getRank()); // Should be rank 1 since it's the only entry
    }
    
    @Test
    @DisplayName("Get user rank should return null if user not found")
    void testGetUserRank_UserNotFound() {
        // Act
        Leaderboard result = leaderboardService.getUserRank("nonexistent");
        
        // Assert
        assertNull(result);
    }
    
    @Test
    @DisplayName("Get top users should return limited number of users")
    void testGetTopUsers() {
        // Arrange - Create and rank leaderboard entries
        leaderboardService.createLeaderboardEntry(user1);
        leaderboardService.createLeaderboardEntry(user2);
        leaderboardService.createLeaderboardEntry(user3);
        leaderboardService.createLeaderboardEntry(user4);
        leaderboardService.calculateRanks();
        
        // Act - Get top 2 users
        List<Leaderboard> result = leaderboardService.getTopUsers(2);
        
        // Assert - Verify correct number and order of results
        assertEquals(2, result.size());
        assertEquals(user2.getId(), result.get(0).getUserId()); // Rank 1 (highest points)
        
        // The second entry could be either user1 or user3 since they're tied
        String secondUserId = result.get(1).getUserId();
        assertTrue(secondUserId.equals(user1.getId()) || secondUserId.equals(user3.getId()));
    }
    
    @Test
    @DisplayName("Update leaderboard entry should update existing entry")
    void testUpdateLeaderboardEntry() {
        // Arrange - Create initial leaderboard entry
        Leaderboard initial = leaderboardService.createLeaderboardEntry(user1);
        assertNotNull(initial);
        
        // Modify user data
        user1.setUsername("updatedUsername");
        user1.setDepartment("updatedDepartment");
        user1.setEarnedPoints(300L);
        userRepository.save(user1);
        
        // Act - Update the leaderboard entry
        Leaderboard result = leaderboardService.updateLeaderboardEntry(user1.getId());
        
        // Assert - Verify entry was updated with new user data
        assertNotNull(result);
        assertEquals("updatedUsername", result.getUsername());
        assertEquals("updatedDepartment", result.getDepartment());
        assertEquals(300L, result.getEarnedPoints());
        assertEquals(level3, result.getCurrentLevel()); // Should be level 3 now (>200 points)
    }
    
    @Test
    @DisplayName("Update leaderboard entry should create new entry if it doesn't exist")
    void testUpdateLeaderboardEntry_CreateNew() {
        // Verify no leaderboard entry exists yet
        assertFalse(leaderboardRepository.existsById(user1.getId()));
        
        // Act - Update (should create new entry)
        Leaderboard result = leaderboardService.updateLeaderboardEntry(user1.getId());
        
        // Assert - Verify entry was created with correct data
        assertNotNull(result);
        assertEquals(user1.getId(), result.getUserId());
        assertEquals(user1.getUsername(), result.getUsername());
        assertEquals(user1.getDepartment(), result.getDepartment());
        assertEquals(user1.getEarnedPoints(), result.getEarnedPoints());
        
        // Verify the entry was saved to the database
        assertTrue(leaderboardRepository.existsById(user1.getId()));
    }
    
    @Test
    @DisplayName("Update leaderboard entry should return null if user not found")
    void testUpdateLeaderboardEntry_UserNotFound() {
        // Act
        Leaderboard result = leaderboardService.updateLeaderboardEntry("nonexistent");
        
        // Assert
        assertNull(result);
    }
}
