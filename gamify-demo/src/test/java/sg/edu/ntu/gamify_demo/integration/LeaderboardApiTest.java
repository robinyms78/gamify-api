package sg.edu.ntu.gamify_demo.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import sg.edu.ntu.gamify_demo.dtos.LeaderboardEntryDTO;
import sg.edu.ntu.gamify_demo.interfaces.LeaderboardService;
import sg.edu.ntu.gamify_demo.models.LadderLevel;
import sg.edu.ntu.gamify_demo.models.Leaderboard;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.enums.UserRole;
import sg.edu.ntu.gamify_demo.repositories.LadderLevelRepository;
import sg.edu.ntu.gamify_demo.repositories.LeaderboardRepository;
import sg.edu.ntu.gamify_demo.repositories.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LeaderboardApiTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private LeaderboardService leaderboardService;
    
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
        
        // Save users first
        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);
        user3 = userRepository.save(user3);
        
        // Create leaderboard entries directly
        Leaderboard lb1 = new Leaderboard();
        lb1.setUserId(user1.getId());
        lb1.setUsername(user1.getUsername());
        lb1.setDepartment(user1.getDepartment());
        lb1.setEarnedPoints(user1.getEarnedPoints());
        lb1.setCurrentLevel(level1);
        lb1.setRank(3L); // Will be recalculated later
        
        Leaderboard lb2 = new Leaderboard();
        lb2.setUserId(user2.getId());
        lb2.setUsername(user2.getUsername());
        lb2.setDepartment(user2.getDepartment());
        lb2.setEarnedPoints(user2.getEarnedPoints());
        lb2.setCurrentLevel(level2);
        lb2.setRank(1L); // Will be recalculated later
        
        Leaderboard lb3 = new Leaderboard();
        lb3.setUserId(user3.getId());
        lb3.setUsername(user3.getUsername());
        lb3.setDepartment(user3.getDepartment());
        lb3.setEarnedPoints(user3.getEarnedPoints());
        lb3.setCurrentLevel(level2);
        lb3.setRank(2L); // Will be recalculated later
        
        // Save leaderboard entries
        leaderboardRepository.save(lb1);
        leaderboardRepository.save(lb2);
        leaderboardRepository.save(lb3);
        
        // Calculate ranks to ensure they're correct
        leaderboardService.calculateRanks();
    }
    
    @Test
    @WithMockUser
    @DisplayName("GET /api/leaderboard/global should return paginated global rankings")
    void getGlobalRankings_ShouldReturnPaginatedGlobalRankings() throws Exception {
        // Act & Assert
        MvcResult result = mockMvc.perform(get("/api/leaderboard/global")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andReturn();
        
        // Parse the response
        String content = result.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {});
        List<Map<String, Object>> contentList = (List<Map<String, Object>>) responseMap.get("content");
        
        // Verify the order (by rank)
        assertEquals("user2", contentList.get(0).get("userId"));
        assertEquals(1, contentList.get(0).get("rank"));
        assertEquals("user3", contentList.get(1).get("userId"));
        assertEquals(2, contentList.get(1).get("rank"));
        assertEquals("user1", contentList.get(2).get("userId"));
        assertEquals(3, contentList.get(2).get("rank"));
    }
    
    @Test
    @WithMockUser
    @DisplayName("GET /api/leaderboard/departments/{department} should return filtered rankings")
    void getDepartmentRankings_ShouldReturnFilteredRankings() throws Exception {
        // Act & Assert
        MvcResult result = mockMvc.perform(get("/api/leaderboard/departments/Engineering")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andReturn();
        
        // Parse the response
        String content = result.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {});
        List<Map<String, Object>> contentList = (List<Map<String, Object>>) responseMap.get("content");
        
        // Verify the order and department
        assertEquals("user2", contentList.get(0).get("userId"));
        assertEquals("Engineering", contentList.get(0).get("department"));
        assertEquals("user1", contentList.get(1).get("userId"));
        assertEquals("Engineering", contentList.get(1).get("department"));
    }
    
    @Test
    @WithMockUser
    @DisplayName("GET /api/leaderboard/users/{userId} should return user's rank")
    void getUserRank_ShouldReturnUserRank() throws Exception {
        // Act & Assert
        MvcResult result = mockMvc.perform(get("/api/leaderboard/users/user1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user1"))
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.department").value("Engineering"))
                .andExpect(jsonPath("$.earnedPoints").value(100))
                .andExpect(jsonPath("$.rank").value(3))
                .andReturn();
        
        // Parse the response
        String content = result.getResponse().getContentAsString();
        LeaderboardEntryDTO dto = objectMapper.readValue(content, LeaderboardEntryDTO.class);
        
        // Verify the DTO
        assertEquals("user1", dto.getUserId());
        assertEquals("user1", dto.getUsername());
        assertEquals("Engineering", dto.getDepartment());
        assertEquals(100, dto.getEarnedPoints());
        assertEquals(3, dto.getRank());
    }
    
    @Test
    @WithMockUser
    @DisplayName("GET /api/leaderboard/users/{userId} should return 404 if user not found")
    void getUserRank_ShouldReturn404IfUserNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/leaderboard/users/nonexistent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }
    
    @Test
    @WithMockUser
    @DisplayName("GET /api/leaderboard/top should return top users")
    void getTopUsers_ShouldReturnTopUsers() throws Exception {
        // Act & Assert
        MvcResult result = mockMvc.perform(get("/api/leaderboard/top")
                .param("limit", "2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andReturn();
        
        // Parse the response
        String content = result.getResponse().getContentAsString();
        List<LeaderboardEntryDTO> dtos = objectMapper.readValue(content, new TypeReference<List<LeaderboardEntryDTO>>() {});
        
        // Verify the order
        assertEquals("user2", dtos.get(0).getUserId());
        assertEquals(1, dtos.get(0).getRank());
        assertEquals("user3", dtos.get(1).getUserId());
        assertEquals(2, dtos.get(1).getRank());
    }
    
    @Test
    @WithMockUser
    @DisplayName("GET /api/leaderboard/recalculate should recalculate ranks")
    void recalculateRanks_ShouldRecalculateRanks() throws Exception {
        // Arrange - Change user points to alter the ranking
        user1.setEarnedPoints(300L); // Now should be rank 1
        userRepository.save(user1);
        
        // Update leaderboard entry with new points
        leaderboardService.updateLeaderboardEntry(user1.getId());
        
        // Act & Assert
        MvcResult result = mockMvc.perform(get("/api/leaderboard/recalculate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        
        // Parse the response
        String content = result.getResponse().getContentAsString();
        int updatedCount = Integer.parseInt(content);
        
        // Verify the count
        assertEquals(3, updatedCount);
        
        // Verify the new ranks
        Leaderboard lb1 = leaderboardRepository.findById(user1.getId()).orElse(null);
        Leaderboard lb2 = leaderboardRepository.findById(user2.getId()).orElse(null);
        Leaderboard lb3 = leaderboardRepository.findById(user3.getId()).orElse(null);
        
        assertNotNull(lb1);
        assertNotNull(lb2);
        assertNotNull(lb3);
        
        assertEquals(1L, lb1.getRank()); // 300 points
        assertEquals(2L, lb2.getRank()); // 200 points
        assertEquals(3L, lb3.getRank()); // 150 points
    }
    
    @Test
    @DisplayName("Endpoints should require authentication")
    void endpoints_ShouldRequireAuthentication() throws Exception {
        // Act & Assert - Without authentication
        mockMvc.perform(get("/api/leaderboard/global")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(get("/api/leaderboard/departments/Engineering")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(get("/api/leaderboard/users/user1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(get("/api/leaderboard/top")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(get("/api/leaderboard/recalculate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
