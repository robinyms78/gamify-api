package sg.edu.ntu.gamify_demo.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import sg.edu.ntu.gamify_demo.models.LadderLevel;
import sg.edu.ntu.gamify_demo.models.Leaderboard;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.enums.UserRole;
import sg.edu.ntu.gamify_demo.repositories.LadderLevelRepository;
import sg.edu.ntu.gamify_demo.repositories.LeaderboardRepository;
import sg.edu.ntu.gamify_demo.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceImplTest {

    @Mock
    private LeaderboardRepository leaderboardRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private LadderLevelRepository ladderLevelRepository;
    
    @InjectMocks
    private LeaderboardServiceImpl leaderboardService;
    
    @Captor
    private ArgumentCaptor<Leaderboard> leaderboardCaptor;
    
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
        // Create test users
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
        
        // Create ladder levels
        level1 = new LadderLevel(1L, "Beginner", 0L);
        level2 = new LadderLevel(2L, "Intermediate", 100L);
        
        // Create leaderboard entries
        leaderboard1 = new Leaderboard(user1, 100L, level2, 3L);
        leaderboard2 = new Leaderboard(user2, 200L, level2, 1L);
        leaderboard3 = new Leaderboard(user3, 150L, level2, 2L);
    }
    
    @Test
    @DisplayName("Calculate ranks should update ranks based on points")
    void calculateRanks_ShouldUpdateRanksBasedOnPoints() {
        // Arrange
        List<Leaderboard> leaderboards = Arrays.asList(leaderboard1, leaderboard2, leaderboard3);
        when(leaderboardRepository.findAll()).thenReturn(leaderboards);
        when(leaderboardRepository.save(any(Leaderboard.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        int updatedCount = leaderboardService.calculateRanks();
        
        // Assert
        assertEquals(3, updatedCount);
        verify(leaderboardRepository, times(3)).save(leaderboardCaptor.capture());
        
        List<Leaderboard> capturedLeaderboards = leaderboardCaptor.getAllValues();
        assertEquals(1L, capturedLeaderboards.get(0).getRank()); // user2 (200 points)
        assertEquals(2L, capturedLeaderboards.get(1).getRank()); // user3 (150 points)
        assertEquals(3L, capturedLeaderboards.get(2).getRank()); // user1 (100 points)
    }
    
    @Test
    @DisplayName("Calculate ranks should handle ties correctly")
    void calculateRanks_ShouldHandleTiesCorrectly() {
        // Arrange
        // Modify user3 to have the same points as user2
        user3.setEarnedPoints(200L);
        leaderboard3.setEarnedPoints(200L);
        
        List<Leaderboard> leaderboards = Arrays.asList(leaderboard1, leaderboard2, leaderboard3);
        when(leaderboardRepository.findAll()).thenReturn(leaderboards);
        when(leaderboardRepository.save(any(Leaderboard.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        int updatedCount = leaderboardService.calculateRanks();
        
        // Assert
        assertEquals(3, updatedCount);
        verify(leaderboardRepository, times(3)).save(leaderboardCaptor.capture());
        
        List<Leaderboard> capturedLeaderboards = leaderboardCaptor.getAllValues();
        
        // Both user2 and user3 should have rank 1, user1 should have rank 3
        assertEquals(1L, capturedLeaderboards.get(0).getRank()); // user2 (200 points)
        assertEquals(1L, capturedLeaderboards.get(1).getRank()); // user3 (200 points)
        assertEquals(3L, capturedLeaderboards.get(2).getRank()); // user1 (100 points)
    }
    
    @Test
    @DisplayName("Get global rankings should return paginated results")
    void getGlobalRankings_ShouldReturnPaginatedResults() {
        // Arrange
        List<Leaderboard> leaderboards = Arrays.asList(leaderboard2, leaderboard3, leaderboard1);
        when(leaderboardRepository.findAllByOrderByRankAsc()).thenReturn(leaderboards);
        
        Pageable pageable = PageRequest.of(0, 2);
        
        // Act
        Page<Leaderboard> result = leaderboardService.getGlobalRankings(pageable);
        
        // Assert
        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalElements());
        assertEquals(leaderboard2, result.getContent().get(0));
        assertEquals(leaderboard3, result.getContent().get(1));
    }
    
    @Test
    @DisplayName("Get department rankings should filter by department")
    void getDepartmentRankings_ShouldFilterByDepartment() {
        // Arrange
        List<Leaderboard> engineeringLeaderboards = Arrays.asList(leaderboard2, leaderboard1);
        when(leaderboardRepository.findByDepartmentOrderByRankAsc("Engineering")).thenReturn(engineeringLeaderboards);
        
        Pageable pageable = PageRequest.of(0, 10);
        
        // Act
        Page<Leaderboard> result = leaderboardService.getDepartmentRankings("Engineering", pageable);
        
        // Assert
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals(leaderboard2, result.getContent().get(0));
        assertEquals(leaderboard1, result.getContent().get(1));
    }
    
    @Test
    @DisplayName("Get user rank should return user's leaderboard entry")
    void getUserRank_ShouldReturnUserLeaderboardEntry() {
        // Arrange
        when(leaderboardRepository.findById("user1")).thenReturn(Optional.of(leaderboard1));
        
        // Act
        Leaderboard result = leaderboardService.getUserRank("user1");
        
        // Assert
        assertNotNull(result);
        assertEquals(leaderboard1, result);
    }
    
    @Test
    @DisplayName("Get user rank should return null if user not found")
    void getUserRank_ShouldReturnNullIfUserNotFound() {
        // Arrange
        when(leaderboardRepository.findById("nonexistent")).thenReturn(Optional.empty());
        
        // Act
        Leaderboard result = leaderboardService.getUserRank("nonexistent");
        
        // Assert
        assertNull(result);
    }
    
    @Test
    @DisplayName("Get top users should return limited number of users")
    void getTopUsers_ShouldReturnLimitedNumberOfUsers() {
        // Arrange
        List<Leaderboard> topUsers = Arrays.asList(leaderboard2, leaderboard3);
        when(leaderboardRepository.findTopUsers(2)).thenReturn(topUsers);
        
        // Act
        List<Leaderboard> result = leaderboardService.getTopUsers(2);
        
        // Assert
        assertEquals(2, result.size());
        assertEquals(leaderboard2, result.get(0));
        assertEquals(leaderboard3, result.get(1));
    }
    
    @Test
    @DisplayName("Create leaderboard entry should create new entry for user")
    void createLeaderboardEntry_ShouldCreateNewEntryForUser() {
        // Arrange
        when(leaderboardRepository.existsById("user1")).thenReturn(false);
        when(ladderLevelRepository.findByLevel(1L)).thenReturn(level1);
        when(leaderboardRepository.count()).thenReturn(0L);
        when(leaderboardRepository.save(any(Leaderboard.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Leaderboard result = leaderboardService.createLeaderboardEntry(user1);
        
        // Assert
        assertNotNull(result);
        assertEquals(user1, result.getUser());
        assertEquals(user1.getEarnedPoints(), result.getEarnedPoints());
        assertEquals(level1, result.getCurrentLevel());
        assertEquals(1L, result.getRank());
        
        verify(leaderboardRepository).save(leaderboardCaptor.capture());
        Leaderboard capturedLeaderboard = leaderboardCaptor.getValue();
        assertEquals(user1.getId(), capturedLeaderboard.getUserId());
        assertEquals(user1.getUsername(), capturedLeaderboard.getUsername());
        assertEquals(user1.getDepartment(), capturedLeaderboard.getDepartment());
    }
    
    @Test
    @DisplayName("Create leaderboard entry should return existing entry if it exists")
    void createLeaderboardEntry_ShouldReturnExistingEntryIfExists() {
        // Arrange
        when(leaderboardRepository.existsById("user1")).thenReturn(true);
        when(leaderboardRepository.findById("user1")).thenReturn(Optional.of(leaderboard1));
        
        // Act
        Leaderboard result = leaderboardService.createLeaderboardEntry(user1);
        
        // Assert
        assertNotNull(result);
        assertEquals(leaderboard1, result);
        
        // Verify that save was not called
        verify(leaderboardRepository, never()).save(any(Leaderboard.class));
    }
    
    @Test
    @DisplayName("Update leaderboard entry should update existing entry")
    void updateLeaderboardEntry_ShouldUpdateExistingEntry() {
        // Arrange
        when(userRepository.findById("user1")).thenReturn(Optional.of(user1));
        when(leaderboardRepository.findById("user1")).thenReturn(Optional.of(leaderboard1));
        
        List<LadderLevel> levels = Arrays.asList(level1, level2);
        when(ladderLevelRepository.findAllByOrderByLevelAsc()).thenReturn(levels);
        when(leaderboardRepository.save(any(Leaderboard.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Leaderboard result = leaderboardService.updateLeaderboardEntry("user1");
        
        // Assert
        assertNotNull(result);
        assertEquals(user1, result.getUser());
        assertEquals(user1.getEarnedPoints(), result.getEarnedPoints());
        assertEquals(level2, result.getCurrentLevel());
        
        verify(leaderboardRepository).save(leaderboardCaptor.capture());
        Leaderboard capturedLeaderboard = leaderboardCaptor.getValue();
        assertEquals(user1.getId(), capturedLeaderboard.getUserId());
    }
    
    @Test
    @DisplayName("Update leaderboard entry should create new entry if it doesn't exist")
    void updateLeaderboardEntry_ShouldCreateNewEntryIfNotExists() {
        // Arrange
        when(userRepository.findById("user1")).thenReturn(Optional.of(user1));
        when(leaderboardRepository.findById("user1")).thenReturn(Optional.empty());
        
        // Mock the createLeaderboardEntry method to return a new leaderboard entry
        when(leaderboardRepository.existsById("user1")).thenReturn(false);
        when(ladderLevelRepository.findByLevel(1L)).thenReturn(level1);
        when(leaderboardRepository.count()).thenReturn(0L);
        when(leaderboardRepository.save(any(Leaderboard.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Leaderboard result = leaderboardService.updateLeaderboardEntry("user1");
        
        // Assert
        assertNotNull(result);
        assertEquals(user1, result.getUser());
        
        verify(leaderboardRepository).save(any(Leaderboard.class));
    }
    
    @Test
    @DisplayName("Update leaderboard entry should return null if user not found")
    void updateLeaderboardEntry_ShouldReturnNullIfUserNotFound() {
        // Arrange
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());
        
        // Act
        Leaderboard result = leaderboardService.updateLeaderboardEntry("nonexistent");
        
        // Assert
        assertNull(result);
        
        // Verify that save was not called
        verify(leaderboardRepository, never()).save(any(Leaderboard.class));
    }
}
