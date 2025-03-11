package sg.edu.ntu.gamify_demo.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sg.edu.ntu.gamify_demo.interfaces.AchievementService;
import sg.edu.ntu.gamify_demo.interfaces.UserAchievementService;
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.models.Achievement;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.UserAchievement;
import sg.edu.ntu.gamify_demo.models.enums.UserRole;
import sg.edu.ntu.gamify_demo.repositories.AchievementRepository;
import sg.edu.ntu.gamify_demo.repositories.UserAchievementRepository;
import sg.edu.ntu.gamify_demo.repositories.UserRepository;

/**
 * Integration tests for the Achievement Service.
 * These tests verify that the Achievement Service works correctly with real repositories.
 */
@SpringBootTest
@ActiveProfiles("test") // Use test profile for database testing
@Transactional
public class AchievementServiceIntegrationTest {
    
    @Autowired
    private AchievementService achievementService;
    
    @Autowired
    private UserAchievementService userAchievementService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AchievementRepository achievementRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserAchievementRepository userAchievementRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private User testUser;
    
    @BeforeEach
    public void setUp() {
        // Clean up repositories
        userAchievementRepository.deleteAll();
        achievementRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create test user
        testUser = new User();
        testUser.setId("test-user-id");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedpassword");
        testUser.setRole(UserRole.EMPLOYEE);
        testUser.setEarnedPoints(100);
        testUser.setAvailablePoints(100);
        userRepository.save(testUser);
    }
    
    @Test
    public void testCreateAndGetAchievement() {
        // Arrange
        String name = "Test Achievement";
        String description = "Test Description";
        ObjectNode criteria = objectMapper.createObjectNode();
        criteria.put("type", "POINTS_THRESHOLD");
        criteria.put("threshold", 50);
        
        // Act
        Achievement createdAchievement = achievementService.createAchievement(name, description, criteria);
        Achievement retrievedAchievement = achievementService.getAchievementById(createdAchievement.getAchievementId());
        
        // Assert
        assertNotNull(retrievedAchievement);
        assertEquals(name, retrievedAchievement.getName());
        assertEquals(description, retrievedAchievement.getDescription());
        assertEquals(criteria.get("type").asText(), retrievedAchievement.getCriteria().get("type").asText());
        assertEquals(criteria.get("threshold").asInt(), retrievedAchievement.getCriteria().get("threshold").asInt());
    }
    
    @Test
    public void testGetAllAchievements() {
        // Arrange
        String name1 = "Achievement 1";
        String description1 = "Description 1";
        ObjectNode criteria1 = objectMapper.createObjectNode();
        criteria1.put("type", "POINTS_THRESHOLD");
        criteria1.put("threshold", 50);
        
        String name2 = "Achievement 2";
        String description2 = "Description 2";
        ObjectNode criteria2 = objectMapper.createObjectNode();
        criteria2.put("type", "TASK_COMPLETION_COUNT");
        criteria2.put("count", 10);
        
        achievementService.createAchievement(name1, description1, criteria1);
        achievementService.createAchievement(name2, description2, criteria2);
        
        // Act
        List<Achievement> achievements = achievementService.getAllAchievements();
        
        // Assert
        assertEquals(2, achievements.size());
        assertTrue(achievements.stream().anyMatch(a -> a.getName().equals(name1)));
        assertTrue(achievements.stream().anyMatch(a -> a.getName().equals(name2)));
    }
    
    @Test
    public void testUpdateAchievement() {
        // Arrange
        String name = "Original Name";
        String description = "Original Description";
        ObjectNode criteria = objectMapper.createObjectNode();
        criteria.put("type", "POINTS_THRESHOLD");
        criteria.put("threshold", 50);
        
        Achievement achievement = achievementService.createAchievement(name, description, criteria);
        
        String newName = "Updated Name";
        String newDescription = "Updated Description";
        ObjectNode newCriteria = objectMapper.createObjectNode();
        newCriteria.put("type", "POINTS_THRESHOLD");
        newCriteria.put("threshold", 100);
        
        // Act
        Achievement updatedAchievement = achievementService.updateAchievement(
                achievement.getAchievementId(), newName, newDescription, newCriteria);
        
        // Assert
        assertEquals(newName, updatedAchievement.getName());
        assertEquals(newDescription, updatedAchievement.getDescription());
        assertEquals(newCriteria.get("threshold").asInt(), updatedAchievement.getCriteria().get("threshold").asInt());
    }
    
    @Test
    public void testAwardAndCheckAchievement() {
        // Arrange
        String name = "Test Achievement";
        String description = "Test Description";
        ObjectNode criteria = objectMapper.createObjectNode();
        criteria.put("type", "POINTS_THRESHOLD");
        criteria.put("threshold", 50);
        
        Achievement achievement = achievementService.createAchievement(name, description, criteria);
        
        ObjectNode metadata = objectMapper.createObjectNode();
        metadata.put("source", "test");
        
        // Act
        UserAchievement userAchievement = userAchievementService.awardAchievement(testUser, achievement, metadata);
        boolean hasAchievement = userAchievementService.hasAchievement(testUser, achievement);
        
        // Assert
        assertNotNull(userAchievement);
        assertTrue(hasAchievement);
        assertEquals(testUser.getId(), userAchievement.getUser().getId());
        assertEquals(achievement.getAchievementId(), userAchievement.getAchievement().getAchievementId());
        assertEquals("test", userAchievement.getMetadata().get("source").asText());
    }
    
    @Test
    public void testGetUserAchievements() {
        // Arrange
        String name1 = "Achievement 1";
        String description1 = "Description 1";
        ObjectNode criteria1 = objectMapper.createObjectNode();
        criteria1.put("type", "POINTS_THRESHOLD");
        criteria1.put("threshold", 50);
        
        String name2 = "Achievement 2";
        String description2 = "Description 2";
        ObjectNode criteria2 = objectMapper.createObjectNode();
        criteria2.put("type", "TASK_COMPLETION_COUNT");
        criteria2.put("count", 10);
        
        Achievement achievement1 = achievementService.createAchievement(name1, description1, criteria1);
        Achievement achievement2 = achievementService.createAchievement(name2, description2, criteria2);
        
        ObjectNode metadata = objectMapper.createObjectNode();
        metadata.put("source", "test");
        
        userAchievementService.awardAchievement(testUser, achievement1, metadata);
        userAchievementService.awardAchievement(testUser, achievement2, metadata);
        
        // Act
        List<UserAchievement> userAchievements = userAchievementService.getUserAchievements(testUser);
        
        // Assert
        assertEquals(2, userAchievements.size());
        assertTrue(userAchievements.stream()
                .anyMatch(ua -> ua.getAchievement().getAchievementId().equals(achievement1.getAchievementId())));
        assertTrue(userAchievements.stream()
                .anyMatch(ua -> ua.getAchievement().getAchievementId().equals(achievement2.getAchievementId())));
    }
    
    @Test
    public void testCountUserAchievements() {
        // Arrange
        String name1 = "Achievement 1";
        String description1 = "Description 1";
        ObjectNode criteria1 = objectMapper.createObjectNode();
        criteria1.put("type", "POINTS_THRESHOLD");
        criteria1.put("threshold", 50);
        
        String name2 = "Achievement 2";
        String description2 = "Description 2";
        ObjectNode criteria2 = objectMapper.createObjectNode();
        criteria2.put("type", "TASK_COMPLETION_COUNT");
        criteria2.put("count", 10);
        
        Achievement achievement1 = achievementService.createAchievement(name1, description1, criteria1);
        Achievement achievement2 = achievementService.createAchievement(name2, description2, criteria2);
        
        ObjectNode metadata = objectMapper.createObjectNode();
        metadata.put("source", "test");
        
        userAchievementService.awardAchievement(testUser, achievement1, metadata);
        userAchievementService.awardAchievement(testUser, achievement2, metadata);
        
        // Act
        long count = userAchievementService.countUserAchievements(testUser);
        
        // Assert
        assertEquals(2, count);
    }
}
