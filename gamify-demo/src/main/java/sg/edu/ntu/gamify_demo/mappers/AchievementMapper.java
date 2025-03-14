package sg.edu.ntu.gamify_demo.mappers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import sg.edu.ntu.gamify_demo.dtos.AchievementDTO;
import sg.edu.ntu.gamify_demo.dtos.UserAchievementDTO;
import sg.edu.ntu.gamify_demo.models.Achievement;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.UserAchievement;

/**
 * Mapper for converting between Achievement entities and DTOs.
 */
@Component
public class AchievementMapper {
    
    /**
     * Converts an Achievement entity to an AchievementDTO.
     * 
     * @param achievement The Achievement entity to convert.
     * @return The converted AchievementDTO.
     */
    public AchievementDTO toDTO(Achievement achievement) {
        if (achievement == null) {
            return null;
        }
        
        return AchievementDTO.builder()
                .id(achievement.getAchievementId())
                .name(achievement.getName())
                .description(achievement.getDescription())
                .earned(false)
                .build();
    }
    
    /**
     * Converts a UserAchievement entity to an AchievementDTO.
     * 
     * @param userAchievement The UserAchievement entity to convert.
     * @return The converted AchievementDTO.
     */
    public AchievementDTO toDTO(UserAchievement userAchievement) {
        if (userAchievement == null) {
            return null;
        }
        
        Achievement achievement = userAchievement.getAchievement();
        
        return AchievementDTO.builder()
                .id(achievement.getAchievementId())
                .name(achievement.getName())
                .description(achievement.getDescription())
                .earnedAt(userAchievement.getEarnedAt().toLocalDateTime())
                .metadata(userAchievement.getMetadata())
                .earned(true)
                .build();
    }
    
    /**
     * Converts a list of Achievement entities to a list of AchievementDTOs.
     * 
     * @param achievements The list of Achievement entities to convert.
     * @return The converted list of AchievementDTOs.
     */
    public List<AchievementDTO> toDTOList(List<Achievement> achievements) {
        if (achievements == null) {
            return null;
        }
        
        return achievements.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Converts a list of UserAchievement entities to a list of AchievementDTOs.
     * 
     * @param userAchievements The list of UserAchievement entities to convert.
     * @return The converted list of AchievementDTOs.
     */
    public List<AchievementDTO> toDTOListFromUserAchievements(List<UserAchievement> userAchievements) {
        if (userAchievements == null) {
            return null;
        }
        
        return userAchievements.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Creates a UserAchievementDTO for a user with their achievements.
     * 
     * @param user The user.
     * @param userAchievements The user's achievements.
     * @param allAchievements All available achievements.
     * @return The UserAchievementDTO.
     */
    public UserAchievementDTO toUserAchievementDTO(User user, List<UserAchievement> userAchievements, List<Achievement> allAchievements) {
        if (user == null) {
            return null;
        }
        
        // Convert user achievements to DTOs
        List<AchievementDTO> earnedAchievementDTOs = toDTOListFromUserAchievements(userAchievements);
        
        // Create a set of earned achievement IDs for quick lookup
        List<String> earnedAchievementIds = userAchievements.stream()
                .map(ua -> ua.getAchievement().getAchievementId())
                .collect(Collectors.toList());
        
        // Convert all achievements to DTOs, marking them as earned if they are in the user's achievements
        List<AchievementDTO> allAchievementDTOs = allAchievements.stream()
                .map(achievement -> {
                    AchievementDTO dto = toDTO(achievement);
                    
                    // If the achievement is earned, find the corresponding earned achievement DTO
                    if (earnedAchievementIds.contains(achievement.getAchievementId())) {
                        AchievementDTO earnedDTO = earnedAchievementDTOs.stream()
                                .filter(a -> a.getId().equals(achievement.getAchievementId()))
                                .findFirst()
                                .orElse(null);
                        
                        if (earnedDTO != null) {
                            dto.setEarned(true);
                            dto.setEarnedAt(earnedDTO.getEarnedAt());
                            dto.setMetadata(earnedDTO.getMetadata());
                        }
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
        
        return UserAchievementDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .achievements(allAchievementDTOs)
                .totalAchievements(allAchievements.size())
                .earnedAchievements(userAchievements.size())
                .build();
    }
}
