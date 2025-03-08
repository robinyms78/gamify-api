package sg.edu.ntu.gamify_demo.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sg.edu.ntu.gamify_demo.exceptions.AchievementNotFoundException;
import sg.edu.ntu.gamify_demo.interfaces.AchievementService;
import sg.edu.ntu.gamify_demo.models.Achievement;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.UserAchievement;
import sg.edu.ntu.gamify_demo.repositories.AchievementRepository;
import sg.edu.ntu.gamify_demo.repositories.UserAchievementRepository;

/**
 * Implementation of the AchievementService interface.
 * Provides methods for managing achievements and user achievements.
 */
@Service
public class AchievementServiceImpl implements AchievementService {

    @Autowired
    private AchievementRepository achievementRepository;
    
    @Autowired
    private UserAchievementRepository userAchievementRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public Achievement createAchievement(String name, String description, JsonNode criteria) {
        Achievement achievement = new Achievement();
        achievement.setAchievementId(UUID.randomUUID().toString());
        achievement.setName(name);
        achievement.setDescription(description);
        achievement.setCriteria(criteria);
        
        return achievementRepository.save(achievement);
    }

    @Override
    public Achievement getAchievementById(String achievementId) {
        return achievementRepository.findById(achievementId)
            .orElseThrow(() -> new AchievementNotFoundException(achievementId));
    }

    @Override
    public Achievement getAchievementByName(String name) {
        return achievementRepository.findByName(name);
    }

    @Override
    public List<Achievement> getAllAchievements() {
        return achievementRepository.findAll();
    }

    @Override
    public Achievement updateAchievement(String achievementId, String name, String description, JsonNode criteria) {
        Achievement achievement = getAchievementById(achievementId);
        
        achievement.setName(name);
        achievement.setDescription(description);
        achievement.setCriteria(criteria);
        
        return achievementRepository.save(achievement);
    }

    @Override
    public void deleteAchievement(String achievementId) {
        // Check if the achievement exists
        if (!achievementRepository.existsById(achievementId)) {
            throw new AchievementNotFoundException(achievementId);
        }
        
        achievementRepository.deleteById(achievementId);
    }

    @Override
    @Transactional
    public UserAchievement awardAchievement(User user, Achievement achievement, JsonNode metadata) {
        // Check if the user already has this achievement
        if (hasAchievement(user, achievement)) {
            return null;
        }
        
        UserAchievement userAchievement = new UserAchievement();
        userAchievement.setUser(user);
        userAchievement.setAchievement(achievement);
        userAchievement.setEarnedAt(LocalDateTime.now());
        userAchievement.setMetadata(metadata);
        
        return userAchievementRepository.save(userAchievement);
    }

    @Override
    public boolean hasAchievement(User user, Achievement achievement) {
        return userAchievementRepository.existsByUserAndAchievement(user, achievement);
    }

    @Override
    public List<UserAchievement> getUserAchievements(User user) {
        return userAchievementRepository.findByUser(user);
    }

    @Override
    public List<UserAchievement> getAchievementUsers(Achievement achievement) {
        return userAchievementRepository.findByAchievement(achievement);
    }

    @Override
    public long countUserAchievements(User user) {
        return userAchievementRepository.countByUser(user);
    }

    @Override
    public boolean checkAchievementCriteria(User user, Achievement achievement) {
        JsonNode criteria = achievement.getCriteria();
        
        if (criteria == null) {
            return false;
        }
        
        // Get the criteria type
        String type = criteria.path("type").asText();
        
        switch (type) {
            case "POINTS_THRESHOLD":
                int threshold = criteria.path("threshold").asInt();
                return user.getEarnedPoints() >= threshold;
                
            case "TASK_COMPLETION_COUNT":
                // This would require integration with a task service
                // For now, we'll return false
                return false;
                
            case "CONSECUTIVE_DAYS":
                // This would require tracking login/activity dates
                // For now, we'll return false
                return false;
                
            default:
                return false;
        }
    }

    @Override
    @Transactional
    public List<UserAchievement> processAchievements(User user, String eventType, JsonNode eventData) {
        List<UserAchievement> newAchievements = new ArrayList<>();
        List<Achievement> allAchievements = achievementRepository.findAll();
        
        for (Achievement achievement : allAchievements) {
            // Skip if the user already has this achievement
            if (hasAchievement(user, achievement)) {
                continue;
            }
            
            // Check if the achievement criteria matches the event
            JsonNode criteria = achievement.getCriteria();
            if (criteria != null && criteria.has("eventType") && 
                criteria.get("eventType").asText().equals(eventType)) {
                
                // Check if the user meets the criteria
                if (checkAchievementCriteria(user, achievement)) {
                    // Create metadata for the achievement
                    ObjectNode metadata = objectMapper.createObjectNode();
                    metadata.put("eventType", eventType);
                    metadata.set("eventData", eventData);
                    
                    // Award the achievement
                    UserAchievement userAchievement = awardAchievement(user, achievement, metadata);
                    if (userAchievement != null) {
                        newAchievements.add(userAchievement);
                    }
                }
            }
        }
        
        return newAchievements;
    }
}
