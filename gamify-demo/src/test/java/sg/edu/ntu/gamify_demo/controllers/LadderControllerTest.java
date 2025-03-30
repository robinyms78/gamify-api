package sg.edu.ntu.gamify_demo.controllers;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.time.ZonedDateTime;
import java.util.Collections;
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
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import sg.edu.ntu.gamify_demo.config.TestSecurityConfig;
import sg.edu.ntu.gamify_demo.dtos.LadderStatusDTO;
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
    private User advancedUser;
    private LadderLevel beginnerLevel;
    private LadderLevel intermediateLevel;
    private LadderLevel advancedLevel;
    private UserLadderStatus testStatus;
    private UserLadderStatus advancedStatus;
    private LadderStatusDTO testStatusDTO;
    private LadderStatusDTO advancedStatusDTO;

    @BeforeEach
    public void setup() {
        // Setup test data for a beginner user
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

        // Setup test data for an advanced user
        advancedUser = User.builder()
                .id("advanced-user-id")
                .username("advanceduser")
                .email("advanced@example.com")
                .passwordHash("hashedpassword")
                .role(UserRole.EMPLOYEE)
                .department("Engineering")
                .earnedPoints(600L)
                .availablePoints(200L)
                .build();

        // Setup ladder levels
        beginnerLevel = new LadderLevel(1L, "Beginner", 0L);
        intermediateLevel = new LadderLevel(2L, "Intermediate", 300L);
        advancedLevel = new LadderLevel(3L, "Advanced", 600L);

        // Setup ladder statuses
        testStatus = new UserLadderStatus(testUser, beginnerLevel, 100L, 100L);
        advancedStatus = new UserLadderStatus(advancedUser, advancedLevel, 600L, 400L);

        // Setup DTOs
        testStatusDTO = LadderStatusDTO.builder()
                .currentLevel(1)
                .levelLabel("Beginner")
                .earnedPoints(100)
                .pointsToNextLevel(100)
                .build();
                
        advancedStatusDTO = LadderStatusDTO.builder()
                .currentLevel(3)
                .levelLabel("Advanced")
                .earnedPoints(600)
                .pointsToNextLevel(400)
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
        mockMvc.perform(get("/api/ladder/levels"))
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
        mockMvc.perform(get("/api/ladder/users/test-user-id"))
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
        mockMvc.perform(get("/api/ladder/users/non-existent-user"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    @DisplayName("Should return ladder status via query parameter")
    public void testGetLadderStatus() throws Exception {
        // Arrange
        when(gamificationFacade.getUserLadderStatus("test-user-id")).thenReturn(testStatusDTO);

        // Act & Assert
        mockMvc.perform(get("/api/ladder/status").param("userId", "test-user-id"))
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
        mockMvc.perform(get("/api/ladder/status").param("userId", "non-existent-user"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    @DisplayName("Should return level label")
    public void testGetLevelLabel() throws Exception {
        // Arrange
        when(ladderService.getLevelLabel(1)).thenReturn("Beginner");

        // Act & Assert
        mockMvc.perform(get("/api/ladder/levels/1/label"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value(1))
                .andExpect(jsonPath("$.label").value("Beginner"));
        
        verify(ladderService, times(1)).getLevelLabel(1);
    }
    
    @Test
    @DisplayName("Should return 'Unknown' for non-existent level label")
    public void testGetLevelLabel_NonExistent() throws Exception {
        // Arrange
        when(ladderService.getLevelLabel(999)).thenReturn("Unknown");

        // Act & Assert
        mockMvc.perform(get("/api/ladder/levels/999/label"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value(999))
                .andExpect(jsonPath("$.label").value("Unknown"));
        
        verify(ladderService, times(1)).getLevelLabel(999);
    }
    
    @Test
    @DisplayName("Should update user ladder status")
    public void testUpdateUserLadderStatus() throws Exception {
        // Arrange
        when(ladderService.updateUserLadderStatus("test-user-id")).thenReturn(testStatus);

        // Act & Assert
        mockMvc.perform(put("/api/ladder/users/test-user-id"))
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
        mockMvc.perform(put("/api/ladder/users/non-existent-user"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"))
                .andExpect(jsonPath("$.message").exists());
        
        verify(ladderService, times(1)).updateUserLadderStatus("non-existent-user");
    }
    
    @Test
    @DisplayName("Should handle empty ladder levels")
    public void testGetLadderLevels_Empty() throws Exception {
        // Arrange
        when(ladderService.getLadderLevels()).thenReturn(Collections.emptyMap());

        // Act & Assert
        mockMvc.perform(get("/api/ladder/levels"))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));
        
        verify(ladderService, times(1)).getLadderLevels();
    }
    
    @Test
    @DisplayName("Should handle advanced user ladder status")
    public void testGetUserLadderStatus_AdvancedUser() throws Exception {
        // Arrange
        when(ladderService.getUserLadderStatus("advanced-user-id")).thenReturn(advancedStatus);

        // Act & Assert
        mockMvc.perform(get("/api/ladder/users/advanced-user-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.earnedPoints").value(600))
                .andExpect(jsonPath("$.pointsToNextLevel").value(400))
                .andExpect(jsonPath("$.currentLevel.level").value(3))
                .andExpect(jsonPath("$.currentLevel.label").value("Advanced"));
        
        verify(ladderService, times(1)).getUserLadderStatus("advanced-user-id");
    }
    
    @Test
    @DisplayName("Should handle advanced user ladder status via query parameter")
    public void testGetLadderStatus_AdvancedUser() throws Exception {
        // Arrange
        when(gamificationFacade.getUserLadderStatus("advanced-user-id")).thenReturn(advancedStatusDTO);

        // Act & Assert
        mockMvc.perform(get("/api/ladder/status").param("userId", "advanced-user-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentLevel").value(3))
                .andExpect(jsonPath("$.levelLabel").value("Advanced"))
                .andExpect(jsonPath("$.earnedPoints").value(600))
                .andExpect(jsonPath("$.pointsToNextLevel").value(400));
        
        verify(gamificationFacade, times(1)).getUserLadderStatus("advanced-user-id");
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
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.level").value(5))
                .andExpect(jsonPath("$.label").value("Expert"))
                .andExpect(jsonPath("$.pointsRequired").value(1000))
                .andExpect(jsonPath("$.createdAt").exists());
        
        verify(ladderService, times(1)).createLadderLevel(5, "Expert", 1000);
    }
    
    @Test
    @DisplayName("Should handle invalid ladder level creation request")
    public void testCreateLadderLevel_InvalidRequest() throws Exception {
        // Arrange - Create a mock controller to handle the exception
        when(ladderService.createLadderLevel(anyInt(), eq(null), anyInt()))
            .thenThrow(new IllegalArgumentException("Missing required fields"));
            
        ObjectNode requestBody = objectMapper.createObjectNode();
        // Missing required fields
        requestBody.put("level", 5);
        // Missing label and pointsRequired
        
        // Act & Assert
        mockMvc.perform(post("/api/ladder/levels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().is5xxServerError());
        
        verify(ladderService, never()).createLadderLevel(anyInt(), anyString(), anyInt());
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
                .content(objectMapper.writeValueAsString(requestBody)))
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
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Should delete a ladder level")
    public void testDeleteLadderLevel() throws Exception {
        // Arrange
        when(ladderService.deleteLadderLevel(4)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/api/ladder/levels/4"))
                .andExpect(status().isNoContent());
    }
    
    @Test
    @DisplayName("Should return conflict when deleting a level with users")
    public void testDeleteLadderLevel_Conflict() throws Exception {
        // Arrange
        when(ladderService.deleteLadderLevel(2)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/api/ladder/levels/2"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Cannot delete level"))
                .andExpect(jsonPath("$.message").value("The level does not exist or users are currently at this level"));
        
        verify(ladderService, times(1)).deleteLadderLevel(2);
    }
    
    @Test
    @DisplayName("Should handle multiple ladder level operations in sequence")
    public void testMultipleLadderLevelOperations() throws Exception {
        // Arrange
        LadderLevel newLevel = new LadderLevel(4L, "Senior", 900L);
        newLevel.setCreatedAt(ZonedDateTime.now());
        
        ObjectNode createRequestBody = objectMapper.createObjectNode();
        createRequestBody.put("level", 4);
        createRequestBody.put("label", "Senior");
        createRequestBody.put("pointsRequired", 900);
        
        ObjectNode updateRequestBody = objectMapper.createObjectNode();
        updateRequestBody.put("label", "Senior Expert");
        updateRequestBody.put("pointsRequired", 950);
        
        LadderLevel updatedLevel = new LadderLevel(4L, "Senior Expert", 950L);
        
        when(ladderService.createLadderLevel(4, "Senior", 900)).thenReturn(newLevel);
        when(ladderService.updateLadderLevel(4, "Senior Expert", 950)).thenReturn(updatedLevel);
        when(ladderService.deleteLadderLevel(4)).thenReturn(true);
        
        // Act & Assert - Create
        mockMvc.perform(post("/api/ladder/levels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.level").value(4))
                .andExpect(jsonPath("$.label").value("Senior"));
        
        // Act & Assert - Update
        mockMvc.perform(put("/api/ladder/levels/4")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value(4))
                .andExpect(jsonPath("$.label").value("Senior Expert"))
                .andExpect(jsonPath("$.pointsRequired").value(950));
        
        // Act & Assert - Delete
        mockMvc.perform(delete("/api/ladder/levels/4"))
                .andExpect(status().isNoContent());
        
        verify(ladderService, times(1)).createLadderLevel(4, "Senior", 900);
        verify(ladderService, times(1)).updateLadderLevel(4, "Senior Expert", 950);
        verify(ladderService, times(1)).deleteLadderLevel(4);
    }
}
