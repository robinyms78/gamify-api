package sg.edu.ntu.gamify_demo.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import sg.edu.ntu.gamify_demo.dtos.LadderStatusDTO;
import sg.edu.ntu.gamify_demo.models.LadderLevel;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.UserLadderStatus;
import sg.edu.ntu.gamify_demo.models.enums.UserRole;
import sg.edu.ntu.gamify_demo.repositories.LadderLevelRepository;
import sg.edu.ntu.gamify_demo.repositories.UserLadderStatusRepository;
import sg.edu.ntu.gamify_demo.repositories.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Use test profile for database testing
@Transactional
@WithMockUser(username = "testuser", roles = {"EMPLOYEE"})
public class LadderStatusIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LadderLevelRepository ladderLevelRepository;

    @Autowired
    private UserLadderStatusRepository userLadderStatusRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private LadderLevel level1;
    private LadderLevel level2;

    @BeforeEach
    public void setup() {
        // Create test ladder levels
        level1 = new LadderLevel();
        level1.setLevel(1L);
        level1.setLabel("Beginner");
        level1.setPointsRequired(0L);
        level1.setCreatedAt(ZonedDateTime.now());
        level1 = ladderLevelRepository.save(level1);

        level2 = new LadderLevel();
        level2.setLevel(2L);
        level2.setLabel("Intermediate");
        level2.setPointsRequired(200L);
        level2.setCreatedAt(ZonedDateTime.now());
        level2 = ladderLevelRepository.save(level2);

        // Create test user
        testUser = new User();
        testUser.setId("test-integration-user");
        testUser.setUsername("testintegration");
        testUser.setEmail("testintegration@example.com");
        testUser.setPasswordHash("hashedpassword");
        testUser.setRole(UserRole.EMPLOYEE);
        testUser.setDepartment("IT");
        testUser.setEarnedPoints(100L);
        testUser.setAvailablePoints(50L);
        testUser = userRepository.save(testUser);

        // Create user ladder status
        UserLadderStatus status = new UserLadderStatus();
        status.setUser(testUser);
        status.setCurrentLevel(level1);
        status.setEarnedPoints(testUser.getEarnedPoints());
        status.setPointsToNextLevel(level2.getPointsRequired() - testUser.getEarnedPoints());
        status.setUpdatedAt(ZonedDateTime.now());
        userLadderStatusRepository.save(status);
    }

    @Test
    public void testGetLadderStatus() throws Exception {
        // Act
        MvcResult result = mockMvc.perform(get("/api/ladder/status")
                .param("userId", testUser.getId())
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentLevel").value(1))
                .andExpect(jsonPath("$.levelLabel").value("Beginner"))
                .andExpect(jsonPath("$.earnedPoints").value(100))
                .andExpect(jsonPath("$.pointsToNextLevel").value(100))
                .andReturn();

        // Assert
        String content = result.getResponse().getContentAsString();
        LadderStatusDTO dto = objectMapper.readValue(content, LadderStatusDTO.class);
        
        assertNotNull(dto);
        assertEquals(1, dto.getCurrentLevel());
        assertEquals("Beginner", dto.getLevelLabel());
        assertEquals(100, dto.getEarnedPoints());
        assertEquals(100, dto.getPointsToNextLevel());
    }

    @Test
    public void testGetLadderStatus_UserNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/ladder/status")
                .param("userId", "non-existent-user")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    public void testLevelUp() throws Exception {
        // Arrange - Update user points to level up
        testUser.setEarnedPoints(250L); // Above level 2 threshold
        userRepository.save(testUser);

        // Get the user's ladder status
        UserLadderStatus status = userLadderStatusRepository.findById(testUser.getId()).orElse(null);
        assertNotNull(status);
        
        // Update the ladder status directly
        status.setEarnedPoints(250L);
        status.setCurrentLevel(level2);
        status.setPointsToNextLevel(0L);
        userLadderStatusRepository.save(status);

        // Get updated status
        MvcResult result = mockMvc.perform(get("/api/ladder/status")
                .param("userId", testUser.getId())
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        String content = result.getResponse().getContentAsString();
        LadderStatusDTO dto = objectMapper.readValue(content, LadderStatusDTO.class);
        
        assertNotNull(dto);
        assertEquals(2, dto.getCurrentLevel());
        assertEquals("Intermediate", dto.getLevelLabel());
        assertEquals(250, dto.getEarnedPoints());
    }}
