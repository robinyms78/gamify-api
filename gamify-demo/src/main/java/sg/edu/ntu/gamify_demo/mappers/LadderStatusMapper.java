package sg.edu.ntu.gamify_demo.mappers;

import org.springframework.stereotype.Component;
import sg.edu.ntu.gamify_demo.dtos.LadderStatusDTO;
import sg.edu.ntu.gamify_demo.models.LadderLevel;
import sg.edu.ntu.gamify_demo.models.UserLadderStatus;

@Component
public class LadderStatusMapper {
    
    // Static instance for backward compatibility
    public static final LadderStatusMapper INSTANCE = new LadderStatusMapper();
    
    /**
     * Converts a UserLadderStatus entity to a LadderStatusDTO.
     * 
     * @param userLadderStatus The UserLadderStatus entity to convert.
     * @return The converted LadderStatusDTO.
     */
    public LadderStatusDTO toDTO(UserLadderStatus userLadderStatus) {
        if (userLadderStatus == null) {
            return null;
        }
        
        LadderStatusDTO dto = new LadderStatusDTO();
        
        // Map currentLevel
        if (userLadderStatus.getCurrentLevel() != null) {
            dto.setCurrentLevel(
                userLadderStatus.getCurrentLevel().getLevel() != null ? 
                userLadderStatus.getCurrentLevel().getLevel().intValue() : null
            );
            dto.setLevelLabel(userLadderStatus.getCurrentLevel().getLabel());
        }
        
        // Map earnedPoints and pointsToNextLevel
        if (userLadderStatus.getEarnedPoints() != null) {
            dto.setEarnedPoints(userLadderStatus.getEarnedPoints().intValue());
        }
        
        if (userLadderStatus.getPointsToNextLevel() != null) {
            dto.setPointsToNextLevel(userLadderStatus.getPointsToNextLevel().intValue());
        }
        
        // Map user information
        if (userLadderStatus.getUser() != null) {
            dto.setUserId(userLadderStatus.getUser().getId());
            dto.setUserName(userLadderStatus.getUser().getUsername());
        }
        
        return dto;
    }
    
    /**
     * Helper method to convert a LadderLevel to an Integer.
     * 
     * @param ladderLevel The LadderLevel to convert.
     * @return The level as an Integer.
     */
    private Integer ladderLevelToInteger(LadderLevel ladderLevel) {
        return ladderLevel != null ? 
            (ladderLevel.getLevel() != null ? ladderLevel.getLevel().intValue() : null) : null;
    }
    
    /**
     * Helper method to convert a Long to an Integer.
     * 
     * @param value The Long value to convert.
     * @return The value as an Integer.
     */
    private Integer longToInteger(Long value) {
        return value != null ? value.intValue() : null;
    }
}
