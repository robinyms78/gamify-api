package sg.edu.ntu.gamify_demo.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sg.edu.ntu.gamify_demo.config.TestSecurityConfig;
import sg.edu.ntu.gamify_demo.dtos.LadderStatusDTO;
import sg.edu.ntu.gamify_demo.exceptions.UserNotFoundException;
import sg.edu.ntu.gamify_demo.facades.GamificationFacade;
import sg.edu.ntu.gamify_demo.models.LadderLevel;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.UserLadderStatus;
import sg.edu.ntu.gamify_demo.models.enums.UserRole;
import sg.edu.ntu.gamify_demo.services.LadderService;

@WebMvcTest(LadderController.class)
@Import(TestSecurityConfig.class)
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
                .earnedPoints(100L)
                .availablePoints(50L)
                .build();

        testLevel = new LadderLevel(1L, "Beginner", 0L);

        testStatus = new UserLadderStatus(testUser, testLevel, 100L, 100L);

        testStatusDTO = LadderStatusDTO.builder()
                .currentLevel(1)
                .levelLabel("Beginner")
                .earnedPoints(100)
                .pointsToNextLevel(100)
                .build();
    }

    @Test
    @DisplayName("Should return all ladder levels")
    public void testGetLadderLevels() throws Exception {
        // Arrange
        Map<Long, Long> levels = new HashMap<>();
        levels.put(1L, 0L);
        levels.put(2L, 200L);
        levels.put(3L, 500L);

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
    @DisplayName("Should return user ladder status")
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
    @DisplayName("Should return 404 when user not found for ladder status")
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
    @DisplayName("Should return ladder status via query parameter")
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
    @DisplayName("Should return 404 when user not found for ladder status query")
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
    @DisplayName("Should return level label")
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
    
    @Test
    @DisplayName("Should update user ladder status")
    public void testUpdateUserLadderStatus() throws Exception {
        // Arrange
        when(ladderService.updateUserLadderStatus("test-user-id")).thenReturn(testStatus);

        // Act & Assert
        mockMvc.perform(put("/api/ladder/users/test-user-id")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.earnedPoints").value(100))
                .andExpect(jsonPath("$.pointsToNextLevel").value(100))
                .andExpect(jsonPath("$.currentLevel.level").value(1))
                .andExpect(jsonPath("$.currentLevel.label").value("Beginner"));
    }
    
    @Test
    @DisplayName("Should return 404 when updating non-existent user")
    public void testUpdateUserLadderStatus_UserNotFound() throws Exception {
        // Arrange
        when(ladderService.updateUserLadderStatus("non-existent-user")).thenReturn(null);

        // Act & Assert
        mockMvc.perform(put("/api/ladder/users/non-existent-user")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }
    
    @Test
    @DisplayName("Should create a new ladder level")
    public void testCreateLadderLevel() throws Exception {
        // Arrange
        LadderLevel newLevel = new LadderLevel(5L, "Expert", 1000L);
        newLevel.setCreatedAt(ZonedDateTime.now());
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("level", 5);
        requestBody.put("label", "Expert");
        requestBody.put("pointsRequired", 1000);
        
        when(ladderService.createLadderLevel(5, "Expert", 1000)).thenReturn(newLevel);

        // Act & Assert
        mockMvc.perform(post("/api/ladder/levels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.level").value(5))
                .andExpect(jsonPath("$.label").value("Expert"))
                .andExpect(jsonPath("$.pointsRequired").value(1000));
    }
    
    @Test
    @DisplayName("Should update an existing ladder level")
    public void testUpdateLadderLevel() throws Exception {
        // Arrange
        LadderLevel updatedLevel = new LadderLevel(3L, "Advanced", 800L);
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("label", "Advanced");
        requestBody.put("pointsRequired", 800);
        
        when(ladderService.updateLadderLevel(3, "Advanced", 800)).thenReturn(updatedLevel);

        // Act & Assert
        mockMvc.perform(put("/api/ladder/levels/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value(3))
                .andExpect(jsonPath("$.label").value("Advanced"))
                .andExpect(jsonPath("$.pointsRequired").value(800));
    }
    
    @Test
    @DisplayName("Should return 404 when updating non-existent level")
    public void testUpdateLadderLevel_NotFound() throws Exception {
        // Arrange
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("label", "Unknown");
        requestBody.put("pointsRequired", 999);
        
        when(ladderService.updateLadderLevel(999, "Unknown", 999)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(put("/api/ladder/levels/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Should delete a ladder level")
    public void testDeleteLadderLevel() throws Exception {
        // Arrange
        when(ladderService.deleteLadderLevel(4)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/api/ladder/levels/4")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());
    }
    
    @Test
    @DisplayName("Should return conflict when deleting a level with users")
    public void testDeleteLadderLevel_Conflict() throws Exception {
        // Arrange
        when(ladderService.deleteLadderLevel(2)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/api/ladder/levels/2")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Cannot delete level"));
    }
}
