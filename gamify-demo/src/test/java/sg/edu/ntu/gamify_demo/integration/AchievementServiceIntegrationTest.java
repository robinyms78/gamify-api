package sg.edu.ntu.gamify_demo.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.dao.DataIntegrityViolationException;

import sg.edu.ntu.gamify_demo.exceptions.AchievementNotFoundException;
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
    private ObjectNode baseCriteria;
    
    @BeforeEach
    public void setUp() {
        userAchievementRepository.deleteAll();
        achievementRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create user with service-generated ID
        testUser = userRepository.save(User.builder()
            .username("testuser")
            .email("test@example.com")
            .passwordHash("hashedpassword")
            .role(UserRole.EMPLOYEE)
            .earnedPoints(100L)
            .availablePoints(100L)
            .build());
        
        // Base criteria for test achievements
        baseCriteria = objectMapper.createObjectNode();
        baseCriteria.put("type", "POINTS_THRESHOLD");
        baseCriteria.put("threshold", 50);
    }
    
    @Test
    public void testCreateAndGetAchievement() {
        // Arrange
        String name = "Test Achievement";
        String description = "Test Description";
        
        // Act
        Achievement created = achievementService.createAchievement(name, description, baseCriteria);
        Achievement retrieved = achievementService.getAchievementById(created.getAchievementId());
        
        // Assert
        assertNotNull(retrieved);
        assertEquals(name, retrieved.getName());
        assertEquals(description, retrieved.getDescription());
        assertEquals(baseCriteria.toString(), retrieved.getCriteria().toString());
    }
    
    @Test
    public void testUpdateAchievement_InvalidIdShouldFail() {
        Assertions.assertThrows(AchievementNotFoundException.class, () -> {
            achievementService.updateAchievement(
                "invalid-id", "New Name", "New Desc", baseCriteria);
        });
    }
    
    @Test
    public void testDuplicateAchievementPrevention() {
        achievementService.createAchievement("Unique", "Desc", baseCriteria);
        
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            // Use the builder pattern instead of direct constructor
            achievementRepository.save(
                Achievement.builder()
                    .name("Unique")
                    .description("Desc")
                    .criteria(baseCriteria)
                    .build()
            );
        });
    }
    
    @Test
    public void testGetAchievement_InvalidIdShouldThrow() {
        Assertions.assertThrows(AchievementNotFoundException.class, () -> {
            achievementService.getAchievementById("non-existent-id");
        });
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
        Achievement original = achievementService.createAchievement(
            "Original", "Desc", baseCriteria);
        
        ObjectNode updatedCriteria = baseCriteria.deepCopy();
        updatedCriteria.put("threshold", 100);
        
        Achievement updated = achievementService.updateAchievement(
            original.getAchievementId(), "Updated", "New Desc", updatedCriteria);
        
        // Verify all fields including full criteria
        Assertions.assertAll(
            () -> assertEquals("Updated", updated.getName()),
            () -> assertEquals("New Desc", updated.getDescription()),
            () -> assertEquals(updatedCriteria.toString(), 
                             updated.getCriteria().toString())
        );
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
