package sg.edu.ntu.gamify_demo.Services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sg.edu.ntu.gamify_demo.dtos.UserAchievementDTO;
import sg.edu.ntu.gamify_demo.exceptions.UserNotFoundException;
import sg.edu.ntu.gamify_demo.factories.UserAchievementFactory;
import sg.edu.ntu.gamify_demo.interfaces.AchievementService;
import sg.edu.ntu.gamify_demo.interfaces.UserAchievementService;
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.mappers.AchievementMapper;
import sg.edu.ntu.gamify_demo.models.Achievement;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.UserAchievement;
import sg.edu.ntu.gamify_demo.repositories.UserAchievementRepository;
import sg.edu.ntu.gamify_demo.strategies.achievement.AchievementCriteriaEvaluator;

/**
 * Implementation of the UserAchievementService interface.
 * Provides methods for managing user achievements.
 */
@Service
public class UserAchievementServiceImpl implements UserAchievementService {

    private final UserAchievementRepository userAchievementRepository;
    private final AchievementService achievementService;
    private final UserService userService;
    private final UserAchievementFactory userAchievementFactory;
    private final AchievementCriteriaEvaluator criteriaEvaluator;
    private final AchievementMapper achievementMapper;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param userAchievementRepository Repository for user achievements.
     * @param achievementService Service for managing achievements.
     * @param userService Service for managing users.
     * @param userAchievementFactory Factory for creating user achievements.
     * @param criteriaEvaluator Evaluator for achievement criteria.
     * @param achievementMapper Mapper for converting between entities and DTOs.
     * @param objectMapper Mapper for JSON objects.
     */
    
    public UserAchievementServiceImpl(
            UserAchievementRepository userAchievementRepository,
            AchievementService achievementService,
            UserService userService,
            UserAchievementFactory userAchievementFactory,
            AchievementCriteriaEvaluator criteriaEvaluator,
            AchievementMapper achievementMapper,
            ObjectMapper objectMapper) {
        this.userAchievementRepository = userAchievementRepository;
        this.achievementService = achievementService;
        this.userService = userService;
        this.userAchievementFactory = userAchievementFactory;
        this.criteriaEvaluator = criteriaEvaluator;
        this.achievementMapper = achievementMapper;
        this.objectMapper = objectMapper;
    }
    
    @Override
    @Transactional
    public UserAchievement awardAchievement(User user, Achievement achievement, JsonNode metadata) {
        // Check if the user already has this achievement
        if (hasAchievement(user, achievement)) {
            return null;
        }
        
        UserAchievement userAchievement = userAchievementFactory.createUserAchievement(user, achievement, metadata);
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
        
        return criteriaEvaluator.evaluateCriteria(user, criteria);
    }

    @Override
    @Transactional
    public List<UserAchievement> processAchievements(User user, String eventType, JsonNode eventData) {
        List<UserAchievement> newAchievements = new ArrayList<>();
        List<Achievement> allAchievements = achievementService.getAllAchievements();
        
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

    @Override
    public UserAchievementDTO getUserAchievementsDTO(String userId) {
        User user = userService.getUserById(userId);
        
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        
        List<UserAchievement> userAchievements = getUserAchievements(user);
        List<Achievement> allAchievements = achievementService.getAllAchievements();
        
        return achievementMapper.toUserAchievementDTO(user, userAchievements, allAchievements);
    }
}
