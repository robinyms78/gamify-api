package sg.edu.ntu.gamify_demo.mappers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import sg.edu.ntu.gamify_demo.dtos.LeaderboardEntryDTO;
import sg.edu.ntu.gamify_demo.models.Leaderboard;

/**
 * Mapper for converting between Leaderboard entities and DTOs.
 */
@Component
public class LeaderboardMapper {
    
    // Static instance for backward compatibility
    public static final LeaderboardMapper INSTANCE = new LeaderboardMapper();
    
    /**
     * Converts a Leaderboard entity to a LeaderboardEntryDTO.
     * 
     * @param leaderboard The Leaderboard entity to convert.
     * @return The converted LeaderboardEntryDTO.
     */
    public LeaderboardEntryDTO toDTO(Leaderboard leaderboard) {
        if (leaderboard == null) {
            return null;
        }
        
        LeaderboardEntryDTO dto = new LeaderboardEntryDTO();
        
        // Map user information
        dto.setUserId(leaderboard.getUserId());
        dto.setUsername(leaderboard.getUsername());
        dto.setDepartment(leaderboard.getDepartment());
        
        // Map points and rank
        if (leaderboard.getEarnedPoints() != null) {
            dto.setEarnedPoints(leaderboard.getEarnedPoints().intValue());
        } else {
            dto.setEarnedPoints(0); // Default to 0 if null
        }
        
        if (leaderboard.getRank() != null) {
            dto.setRank(leaderboard.getRank().intValue());
        } else {
            dto.setRank(0); // Default to 0 if null
        }
        
        // Map level information with improved null safety
        if (leaderboard.getCurrentLevel() != null) {
            if (leaderboard.getCurrentLevel().getLevel() != null) {
                dto.setCurrentLevel(leaderboard.getCurrentLevel().getLevel().intValue());
            } else {
                dto.setCurrentLevel(1); // Default to level 1 if null
            }
            
            if (leaderboard.getCurrentLevel().getLabel() != null) {
                dto.setLevelLabel(leaderboard.getCurrentLevel().getLabel());
            } else {
                dto.setLevelLabel("Beginner"); // Default label if null
            }
        } else {
            // Set default values if currentLevel is null
            dto.setCurrentLevel(1);
            dto.setLevelLabel("Beginner");
        }
        
        return dto;
    }
    
    /**
     * Converts a list of Leaderboard entities to a list of LeaderboardEntryDTOs.
     * 
     * @param leaderboards The list of Leaderboard entities to convert.
     * @return The list of converted LeaderboardEntryDTOs.
     */
    public List<LeaderboardEntryDTO> toDTOList(List<Leaderboard> leaderboards) {
        if (leaderboards == null) {
            return null;
        }
        
        return leaderboards.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Converts a Page of Leaderboard entities to a Page of LeaderboardEntryDTOs.
     * 
     * @param leaderboardPage The Page of Leaderboard entities to convert.
     * @param pageable The pagination information.
     * @return The Page of converted LeaderboardEntryDTOs.
     */
    public Page<LeaderboardEntryDTO> toDTOPage(Page<Leaderboard> leaderboardPage, Pageable pageable) {
        if (leaderboardPage == null) {
            return null;
        }
        
        List<LeaderboardEntryDTO> dtos = toDTOList(leaderboardPage.getContent());
        return new PageImpl<>(dtos, pageable, leaderboardPage.getTotalElements());
    }
}
