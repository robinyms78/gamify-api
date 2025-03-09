package sg.edu.ntu.gamify_demo.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

import sg.edu.ntu.gamify_demo.exceptions.AchievementNotFoundException;
import sg.edu.ntu.gamify_demo.factories.AchievementFactory;
import sg.edu.ntu.gamify_demo.interfaces.AchievementService;
import sg.edu.ntu.gamify_demo.models.Achievement;
import sg.edu.ntu.gamify_demo.repositories.AchievementRepository;

/**
 * Implementation of the AchievementService interface.
 * Provides methods for managing achievements.
 */
@Service
public class AchievementServiceImpl implements AchievementService {

    private final AchievementRepository achievementRepository;
    private final AchievementFactory achievementFactory;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param achievementRepository Repository for achievements.
     * @param achievementFactory Factory for creating achievements.
     */
    @Autowired
    public AchievementServiceImpl(AchievementRepository achievementRepository, AchievementFactory achievementFactory) {
        this.achievementRepository = achievementRepository;
        this.achievementFactory = achievementFactory;
    }
    
    @Override
    public Achievement createAchievement(String name, String description, JsonNode criteria) {
        Achievement achievement = achievementFactory.createAchievement(name, description, criteria);
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
}
