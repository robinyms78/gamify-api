package sg.edu.ntu.gamify_demo.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import sg.edu.ntu.gamify_demo.dtos.LeaderboardEntryDTO;
import sg.edu.ntu.gamify_demo.exceptions.UserNotFoundException;
import sg.edu.ntu.gamify_demo.interfaces.LeaderboardService;
import sg.edu.ntu.gamify_demo.mappers.LeaderboardMapper;
import sg.edu.ntu.gamify_demo.models.LadderLevel;
import sg.edu.ntu.gamify_demo.models.Leaderboard;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.enums.UserRole;

@ExtendWith(MockitoExtension.class)
class LeaderboardControllerTest {

    @Mock
    private LeaderboardService leaderboardService;
    
    @Mock
    private LeaderboardMapper leaderboardMapper;
    
    @InjectMocks
    private LeaderboardController leaderboardController;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    
    private User user1;
    private User user2;
    private LadderLevel level1;
    private LadderLevel level2;
    private Leaderboard leaderboard1;
    private Leaderboard leaderboard2;
    private LeaderboardEntryDTO dto1;
    private LeaderboardEntryDTO dto2;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(leaderboardController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .alwaysDo(result -> {
                    if (result.getResolvedException() != null) {
                        System.err.println("Exception during test: " + result.getResolvedException().getMessage());
                        result.getResolvedException().printStackTrace();
                    }
                    if (result.getResponse().getStatus() >= 400) {
                        System.err.println("Error response: " + result.getResponse().getContentAsString());
                    }
                })
                .build();
        
        objectMapper = new ObjectMapper();
        
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
                .department("Marketing")
                .earnedPoints(200L)
                .availablePoints(200L)
                .build();
        
        // Create ladder levels
        level1 = new LadderLevel(1L, "Beginner", 0L);
        level2 = new LadderLevel(2L, "Intermediate", 100L);
        
        // Create leaderboard entries
        leaderboard1 = new Leaderboard(user1, 100L, level1, 2L);
        leaderboard2 = new Leaderboard(user2, 200L, level2, 1L);
        
        // Create DTOs
        dto1 = LeaderboardEntryDTO.builder()
                .userId("user1")
                .username("user1")
                .department("Engineering")
                .earnedPoints(100)
                .currentLevel(1)
                .levelLabel("Beginner")
                .rank(2)
                .build();
        
        dto2 = LeaderboardEntryDTO.builder()
                .userId("user2")
                .username("user2")
                .department("Marketing")
                .earnedPoints(200)
                .currentLevel(2)
                .levelLabel("Intermediate")
                .rank(1)
                .build();
    }
    
    @Test
    @DisplayName("Get global rankings should return paginated results")
    void getGlobalRankings_ShouldReturnPaginatedResults() {
        // Arrange
        List<Leaderboard> leaderboards = Arrays.asList(leaderboard2, leaderboard1);
        Page<Leaderboard> leaderboardPage = new PageImpl<>(leaderboards);
        
        // Debug print
        System.out.println("Test setup - leaderboardPage: " + leaderboardPage);
        
        // Mock service behavior
        when(leaderboardService.getGlobalRankings(any(Pageable.class))).thenReturn(leaderboardPage);
        
        // Act - Call the controller method directly
        ResponseEntity<Page<LeaderboardEntryDTO>> response = 
            leaderboardController.getGlobalRankings(0, 10);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        
        // Verify the content of the response
        List<LeaderboardEntryDTO> content = response.getBody().getContent();
        assertEquals("user2", content.get(0).getUserId());
        assertEquals(1, content.get(0).getRank());
        assertEquals("user1", content.get(1).getUserId());
        assertEquals(2, content.get(1).getRank());
    }
    
    @Test
    @DisplayName("Get department rankings should return filtered results")
    void getDepartmentRankings_ShouldReturnFilteredResults() {
        // Arrange
        List<Leaderboard> engineeringLeaderboards = Arrays.asList(leaderboard1);
        Page<Leaderboard> leaderboardPage = new PageImpl<>(engineeringLeaderboards);
        
        // Debug print
        System.out.println("Test setup - department leaderboardPage: " + leaderboardPage);
        
        // Mock service behavior
        when(leaderboardService.getDepartmentRankings(eq("Engineering"), any(Pageable.class))).thenReturn(leaderboardPage);
        
        // Act - Call the controller method directly
        ResponseEntity<Page<LeaderboardEntryDTO>> response = 
            leaderboardController.getDepartmentRankings("Engineering", 0, 10);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        
        // Verify the content of the response
        List<LeaderboardEntryDTO> content = response.getBody().getContent();
        assertEquals("user1", content.get(0).getUserId());
        assertEquals("Engineering", content.get(0).getDepartment());
    }
    
    @Test
    @DisplayName("Get user rank should return user's leaderboard entry")
    void getUserRank_ShouldReturnUserLeaderboardEntry() throws Exception {
        // Arrange
        when(leaderboardService.getUserRank("user1")).thenReturn(leaderboard1);
        when(leaderboardMapper.toDTO(leaderboard1)).thenReturn(dto1);
        
        // Act & Assert
        mockMvc.perform(get("/api/leaderboard/users/user1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user1"))
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.rank").value(2));
    }
    
    @Test
    @DisplayName("Get user rank should return 404 if user not found")
    void getUserRank_ShouldReturn404IfUserNotFound() throws Exception {
        // Arrange
        when(leaderboardService.getUserRank("nonexistent")).thenReturn(null);
        
        // Act & Assert
        mockMvc.perform(get("/api/leaderboard/users/nonexistent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }
    
    @Test
    @DisplayName("Get top users should return limited number of users")
    void getTopUsers_ShouldReturnLimitedNumberOfUsers() throws Exception {
        // Arrange
        List<Leaderboard> topUsers = Arrays.asList(leaderboard2, leaderboard1);
        List<LeaderboardEntryDTO> dtos = Arrays.asList(dto2, dto1);
        
        when(leaderboardService.getTopUsers(5)).thenReturn(topUsers);
        when(leaderboardMapper.toDTOList(topUsers)).thenReturn(dtos);
        
        // Act & Assert
        mockMvc.perform(get("/api/leaderboard/top")
                .param("limit", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value("user2"))
                .andExpect(jsonPath("$[0].rank").value(1))
                .andExpect(jsonPath("$[1].userId").value("user1"))
                .andExpect(jsonPath("$[1].rank").value(2));
    }
    
    @Test
    @DisplayName("Recalculate ranks should return number of updated entries")
    void recalculateRanks_ShouldReturnNumberOfUpdatedEntries() throws Exception {
        // Arrange
        when(leaderboardService.calculateRanks()).thenReturn(10);
        
        // Act & Assert
        mockMvc.perform(get("/api/leaderboard/recalculate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(10));
    }
    
    @Test
    @DisplayName("Handle user not found exception should return 404 with error message")
    void handleUserNotFoundException_ShouldReturn404WithErrorMessage() throws Exception {
        // Arrange
        when(leaderboardService.getUserRank("nonexistent")).thenThrow(new UserNotFoundException("User not found with id: nonexistent"));
        
        // Act & Assert
        mockMvc.perform(get("/api/leaderboard/users/nonexistent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"))
                .andExpect(jsonPath("$.message").value("User not found with id: nonexistent"));
    }
}
