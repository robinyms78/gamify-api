package sg.edu.ntu.gamify_demo.controllers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import sg.edu.ntu.gamify_demo.dtos.LadderStatusDTO;
import sg.edu.ntu.gamify_demo.exceptions.UserNotFoundException;
import sg.edu.ntu.gamify_demo.facades.GamificationFacade;
import sg.edu.ntu.gamify_demo.models.LadderLevel;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.UserLadderStatus;
import sg.edu.ntu.gamify_demo.models.enums.UserRole;
import sg.edu.ntu.gamify_demo.services.LadderService;

@WebMvcTest(LadderController.class)
@WithMockUser(username = "testuser", roles = {"EMPLOYEE"})
public class LadderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LadderService ladderService;

    @MockBean
    private GamificationFacade gamificationFacade;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private LadderLevel testLevel;
    private UserLadderStatus testStatus;
    private LadderStatusDTO testStatusDTO;

    @BeforeEach
    public void setup() {
        // Setup test data
        testUser = User.builder()
                .id("test-user-id")
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .role(UserRole.EMPLOYEE)
                .department("IT")
                .earnedPoints(100)
                .availablePoints(50)
                .build();

        testLevel = new LadderLevel(1, "Beginner", 0);

        testStatus = new UserLadderStatus(testUser, testLevel, 100, 100);

        testStatusDTO = LadderStatusDTO.builder()
                .currentLevel(1)
                .levelLabel("Beginner")
                .earnedPoints(100)
                .pointsToNextLevel(100)
                .build();
    }

    @Test
    public void testGetLadderLevels() throws Exception {
        // Arrange
        Map<Integer, Integer> levels = new HashMap<>();
        levels.put(1, 0);
        levels.put(2, 200);
        levels.put(3, 500);

        when(ladderService.getLadderLevels()).thenReturn(levels);

        // Act & Assert
        mockMvc.perform(get("/api/ladder/levels")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.1").value(0))
                .andExpect(jsonPath("$.2").value(200))
                .andExpect(jsonPath("$.3").value(500));
    }

    @Test
    public void testGetUserLadderStatus() throws Exception {
        // Arrange
        when(ladderService.getUserLadderStatus("test-user-id")).thenReturn(testStatus);

        // Act & Assert
        mockMvc.perform(get("/api/ladder/users/test-user-id")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.earnedPoints").value(100))
                .andExpect(jsonPath("$.pointsToNextLevel").value(100))
                .andExpect(jsonPath("$.currentLevel.level").value(1))
                .andExpect(jsonPath("$.currentLevel.label").value("Beginner"));
    }

    @Test
    public void testGetUserLadderStatus_UserNotFound() throws Exception {
        // Arrange
        when(ladderService.getUserLadderStatus("non-existent-user")).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/ladder/users/non-existent-user")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    public void testGetLadderStatus() throws Exception {
        // Arrange
        when(gamificationFacade.getUserLadderStatus("test-user-id")).thenReturn(testStatusDTO);

        // Act & Assert
        mockMvc.perform(get("/api/ladder/status").param("userId", "test-user-id")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentLevel").value(1))
                .andExpect(jsonPath("$.levelLabel").value("Beginner"))
                .andExpect(jsonPath("$.earnedPoints").value(100))
                .andExpect(jsonPath("$.pointsToNextLevel").value(100));
    }

    @Test
    public void testGetLadderStatus_UserNotFound() throws Exception {
        // Arrange
        when(gamificationFacade.getUserLadderStatus("non-existent-user")).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/ladder/status").param("userId", "non-existent-user")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    public void testGetLevelLabel() throws Exception {
        // Arrange
        when(ladderService.getLevelLabel(1)).thenReturn("Beginner");

        // Act & Assert
        mockMvc.perform(get("/api/ladder/levels/1/label")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value(1))
                .andExpect(jsonPath("$.label").value("Beginner"));
    }
}
