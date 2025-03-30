package sg.edu.ntu.gamify_demo.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sg.edu.ntu.gamify_demo.models.Achievement;
import sg.edu.ntu.gamify_demo.repositories.AchievementRepository;

/**
 * This test class is dedicated to testing the unique constraint on achievement names.
 * It is isolated from other tests to avoid transaction issues.
 */
@SpringBootTest
@ActiveProfiles("test")
public class AchievementDuplicateNameTest {
    
    @Autowired
    private AchievementRepository achievementRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private ObjectNode baseCriteria;
    
    @BeforeEach
    public void setUp() {
        // Clean up any existing data
        achievementRepository.deleteAll();
        
        baseCriteria = objectMapper.createObjectNode();
        baseCriteria.put("type", "POINTS_THRESHOLD");
        baseCriteria.put("threshold", 50);
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up after test
        achievementRepository.deleteAll();
    }
    
    @Test
    public void testDuplicateAchievementPrevention() {
        // Create a unique name for this test to avoid conflicts with other tests
        String uniqueName = "DuplicateTest-" + UUID.randomUUID().toString().substring(0, 8);
        
        // First, create and save an achievement with this name
        Achievement achievement1 = Achievement.builder()
            .name(uniqueName)
            .description("First achievement")
            .criteria(baseCriteria)
            .build();
        
        achievementRepository.save(achievement1);
        achievementRepository.flush();
        
        // Now try to create another achievement with the same name
        Achievement achievement2 = Achievement.builder()
            .name(uniqueName)
            .description("Second achievement with same name")
            .criteria(baseCriteria)
            .build();
        
        // This should throw an exception
        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            achievementRepository.save(achievement2);
            achievementRepository.flush();
        });
        
        // Verify it's the right type of exception
        boolean isCorrectException = exception instanceof DataIntegrityViolationException 
            || (exception.getCause() != null && exception.getCause() instanceof DataIntegrityViolationException)
            || (exception.getCause() != null && exception.getCause().getCause() != null 
                && exception.getCause().getCause() instanceof DataIntegrityViolationException);
        
        assertTrue(isCorrectException, 
            "Expected DataIntegrityViolationException but got: " + exception.getClass().getName());
    }
}
