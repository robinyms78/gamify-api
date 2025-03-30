package sg.edu.ntu.gamify_demo.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sg.edu.ntu.gamify_demo.dtos.ErrorResponseDTO;
import sg.edu.ntu.gamify_demo.dtos.LadderStatusDTO;
import sg.edu.ntu.gamify_demo.exceptions.UserNotFoundException;
import sg.edu.ntu.gamify_demo.facades.GamificationFacade;
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.mappers.LadderStatusMapper;
import sg.edu.ntu.gamify_demo.models.LadderLevel;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.UserLadderStatus;
import sg.edu.ntu.gamify_demo.models.enums.UserRole;
import sg.edu.ntu.gamify_demo.services.LadderService;

@ExtendWith(MockitoExtension.class)
class LadderControllerTest {

    @Mock
    private LadderService ladderService;
    
    @Mock
    private GamificationFacade gamificationFacade;
    
    @Mock
    private UserService userService;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @InjectMocks
    private LadderController ladderController;
    
    private MockMvc mockMvc;
    
    private User testUser1;
    private User testUser2;
    private LadderLevel level1;
    private LadderLevel level2;
    private UserLadderStatus userLadderStatus1;
    private UserLadderStatus userLadderStatus2;
    private LadderStatusDTO ladderStatusDTO1;
    private LadderStatusDTO ladderStatusDTO2;
    private ObjectNode mockObjectNode;
    
    @BeforeEach
    void setUp() {
        // Setup MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(ladderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        
        // Create test users
        testUser1 = User.builder()
                .id("user1")
                .username("testuser1")
                .email("test1@example.com")
                .passwordHash("hashedpassword1")
                .role(UserRole.EMPLOYEE)
                .department("Engineering")
                .earnedPoints(100L)
                .availablePoints(100L)
                .build();
        
        testUser2 = User.builder()
                .id("user2")
                .username("testuser2")
                .email("test2@example.com")
                .passwordHash("hashedpassword2")
                .role(UserRole.MANAGER)
                .department("Marketing")
                .earnedPoints(200L)
                .availablePoints(150L)
                .build();
        
        // Create ladder levels
        level1 = new LadderLevel(1L, "Beginner", 0L);
        level2 = new LadderLevel(2L, "Intermediate", 100L);
        
        // Create user ladder status
        userLadderStatus1 = new UserLadderStatus(testUser1, level1, 100L, 0L);
        userLadderStatus2 = new UserLadderStatus(testUser2, level2, 200L, 100L);
        
        // Create ladder status DTOs
        ladderStatusDTO1 = LadderStatusDTO.builder()
                .userId("user1")
                .userName("testuser1")
                .currentLevel(1)
                .levelLabel("Beginner")
                .earnedPoints(100)
                .pointsToNextLevel(0)
                .build();
        
        ladderStatusDTO2 = LadderStatusDTO.builder()
                .userId("user2")
                .userName("testuser2")
                .currentLevel(2)
                .levelLabel("Intermediate")
                .earnedPoints(200)
                .pointsToNextLevel(100)
                .build();
    }
    
    @Test
    @DisplayName("Get ladder levels should return all levels")
    void getLadderLevels_ShouldReturnAllLevels() {
        // Arrange
        Map<Long, Long> levels = new HashMap<>();
        levels.put(1L, 0L);
        levels.put(2L, 100L);
        levels.put(3L, 300L);
        
        when(ladderService.getLadderLevels()).thenReturn(levels);
        
        // Act
        ResponseEntity<Map<Long, Long>> response = ladderController.getLadderLevels();
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        assertEquals(0L, response.getBody().get(1L));
        assertEquals(100L, response.getBody().get(2L));
        assertEquals(300L, response.getBody().get(3L));
    }
    
    @Test
    @DisplayName("Get user ladder status by path variable should return status")
    void getUserLadderStatus_ShouldReturnStatus() throws Exception {
        // Arrange
        when(ladderService.getUserLadderStatus("user1")).thenReturn(userLadderStatus1);
        
        // Act & Assert
        mockMvc.perform(get("/api/ladder/users/user1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user1"))
                .andExpect(jsonPath("$.userName").value("testuser1"))
                .andExpect(jsonPath("$.currentLevel").value(1))
                .andExpect(jsonPath("$.levelLabel").value("Beginner"));
    }
    
    @Test
    @DisplayName("Get user ladder status by path variable should handle user not found")
    void getUserLadderStatus_ShouldHandleUserNotFound() throws Exception {
        // Arrange
        when(ladderService.getUserLadderStatus("nonexistent")).thenReturn(null);
        when(userService.getUserById("nonexistent")).thenReturn(null);
        
        // Act & Assert
        mockMvc.perform(get("/api/ladder/users/nonexistent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }
    
    @Test
    @DisplayName("Get user ladder status by path variable should initialize status if user exists but has no status")
    void getUserLadderStatus_ShouldInitializeStatusIfUserExistsButHasNoStatus() throws Exception {
        // Arrange
        when(ladderService.getUserLadderStatus("user1")).thenReturn(null);
        when(userService.getUserById("user1")).thenReturn(testUser1);
        when(ladderService.initializeUserLadderStatus(testUser1)).thenReturn(userLadderStatus1);
        
        // Act & Assert
        mockMvc.perform(get("/api/ladder/users/user1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user1"))
                .andExpect(jsonPath("$.userName").value("testuser1"))
                .andExpect(jsonPath("$.currentLevel").value(1))
                .andExpect(jsonPath("$.levelLabel").value("Beginner"));
    }
    
    @Test
    @DisplayName("Get user ladder status by path variable should handle database error")
    void getUserLadderStatus_ShouldHandleDatabaseError() throws Exception {
        // Arrange
        DataAccessException dataAccessException = mock(DataAccessException.class);
        when(dataAccessException.getMessage()).thenReturn("Database error");
        when(ladderService.getUserLadderStatus("user1")).thenThrow(dataAccessException);
        
        // Act & Assert
        mockMvc.perform(get("/api/ladder/users/user1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Database error"));
    }
    
    @Test
    @DisplayName("Get ladder status by query parameter should return status")
    void getLadderStatus_ShouldReturnStatus() throws Exception {
        // Arrange
        when(gamificationFacade.getUserLadderStatus("user1")).thenReturn(ladderStatusDTO1);
        
        // Act & Assert
        mockMvc.perform(get("/api/ladder/status")
                .param("userId", "user1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user1"))
                .andExpect(jsonPath("$.userName").value("testuser1"))
                .andExpect(jsonPath("$.currentLevel").value(1))
                .andExpect(jsonPath("$.levelLabel").value("Beginner"));
    }
    
    @Test
    @DisplayName("Get ladder status by query parameter should handle user not found")
    void getLadderStatus_ShouldHandleUserNotFound() throws Exception {
        // Arrange
        when(gamificationFacade.getUserLadderStatus("nonexistent")).thenReturn(null);
        
        // Act & Assert
        mockMvc.perform(get("/api/ladder/status")
                .param("userId", "nonexistent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }
    
    @Test
    @DisplayName("Update user ladder status should return updated status")
    void updateUserLadderStatus_ShouldReturnUpdatedStatus() throws Exception {
        // Arrange
        when(ladderService.updateUserLadderStatus("user1")).thenReturn(userLadderStatus1);
        
        // Act & Assert
        mockMvc.perform(put("/api/ladder/users/user1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user1"))
                .andExpect(jsonPath("$.userName").value("testuser1"))
                .andExpect(jsonPath("$.currentLevel").value(1))
                .andExpect(jsonPath("$.levelLabel").value("Beginner"));
    }
    
    @Test
    @DisplayName("Update user ladder status should handle user not found")
    void updateUserLadderStatus_ShouldHandleUserNotFound() throws Exception {
        // Arrange
        when(ladderService.updateUserLadderStatus("nonexistent")).thenReturn(null);
        
        // Act & Assert
        mockMvc.perform(put("/api/ladder/users/nonexistent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }
    
    @Test
    @DisplayName("Get level label should return label")
    void getLevelLabel_ShouldReturnLabel() throws Exception {
        // Arrange
        when(ladderService.getLevelLabel(1L)).thenReturn("Beginner");
        
        // Create a real ObjectNode for the response
        ObjectNode responseNode = new ObjectMapper().createObjectNode();
        responseNode.put("level", 1L);
        responseNode.put("label", "Beginner");
        
        // Mock the ObjectMapper behavior
        when(objectMapper.createObjectNode()).thenReturn(responseNode);
        
        // Act & Assert
        mockMvc.perform(get("/api/ladder/levels/1/label")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Create ladder level should return created level")
    void createLadderLevel_ShouldReturnCreatedLevel() throws Exception {
        // Arrange
        ObjectMapper realObjectMapper = new ObjectMapper();
        ObjectNode levelData = realObjectMapper.createObjectNode();
        levelData.put("level", 3);
        levelData.put("label", "Advanced");
        levelData.put("pointsRequired", 300);
        
        LadderLevel newLevel = new LadderLevel(3L, "Advanced", 300L);
        
        when(ladderService.createLadderLevel(3L, "Advanced", 300L)).thenReturn(newLevel);
        
        // Act & Assert
        mockMvc.perform(post("/api/ladder/levels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(realObjectMapper.writeValueAsString(levelData)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.level").value(3))
                .andExpect(jsonPath("$.label").value("Advanced"))
                .andExpect(jsonPath("$.pointsRequired").value(300));
    }
    
    @Test
    @DisplayName("Update ladder level should return updated level")
    void updateLadderLevel_ShouldReturnUpdatedLevel() throws Exception {
        // Arrange
        ObjectMapper realObjectMapper = new ObjectMapper();
        ObjectNode levelData = realObjectMapper.createObjectNode();
        levelData.put("label", "Updated Beginner");
        levelData.put("pointsRequired", 50);
        
        LadderLevel updatedLevel = new LadderLevel(1L, "Updated Beginner", 50L);
        
        when(ladderService.updateLadderLevel(1L, "Updated Beginner", 50L)).thenReturn(updatedLevel);
        
        // Act & Assert
        mockMvc.perform(put("/api/ladder/levels/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(realObjectMapper.writeValueAsString(levelData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value(1))
                .andExpect(jsonPath("$.label").value("Updated Beginner"))
                .andExpect(jsonPath("$.pointsRequired").value(50));
    }
    
    @Test
    @DisplayName("Update ladder level should handle level not found")
    void updateLadderLevel_ShouldHandleLevelNotFound() throws Exception {
        // Arrange
        ObjectMapper realObjectMapper = new ObjectMapper();
        ObjectNode levelData = realObjectMapper.createObjectNode();
        levelData.put("label", "Nonexistent Level");
        levelData.put("pointsRequired", 999);
        
        when(ladderService.updateLadderLevel(999L, "Nonexistent Level", 999L)).thenReturn(null);
        
        // Act & Assert
        mockMvc.perform(put("/api/ladder/levels/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(realObjectMapper.writeValueAsString(levelData)))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Delete ladder level should return no content when successful")
    void deleteLadderLevel_ShouldReturnNoContentWhenSuccessful() throws Exception {
        // Arrange
        when(ladderService.deleteLadderLevel(1L)).thenReturn(true);
        
        // Act & Assert
        mockMvc.perform(delete("/api/ladder/levels/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
    
    @Test
    @DisplayName("Delete ladder level should handle level not found or in use")
    void deleteLadderLevel_ShouldHandleLevelNotFoundOrInUse() throws Exception {
        // Arrange
        when(ladderService.deleteLadderLevel(1L)).thenReturn(false);
        
        // Act & Assert
        mockMvc.perform(delete("/api/ladder/levels/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Cannot delete level"));
    }
    
    @Test
    @DisplayName("Handle user not found exception should return 404 with error message")
    void handleUserNotFoundException_ShouldReturn404WithErrorMessage() {
        // Arrange
        UserNotFoundException exception = new UserNotFoundException("User not found with id: nonexistent");
        
        // Act
        ResponseEntity<ErrorResponseDTO> response = ladderController.handleUserNotFoundException(exception);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody().getError());
        assertEquals("User not found with id: nonexistent", response.getBody().getMessage());
    }
}
