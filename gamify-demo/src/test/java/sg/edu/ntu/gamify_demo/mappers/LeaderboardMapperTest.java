package sg.edu.ntu.gamify_demo.mappers;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import sg.edu.ntu.gamify_demo.dtos.LeaderboardEntryDTO;
import sg.edu.ntu.gamify_demo.models.LadderLevel;
import sg.edu.ntu.gamify_demo.models.Leaderboard;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.enums.UserRole;

class LeaderboardMapperTest {
    
    private LeaderboardMapper mapper;
    private User user;
    private LadderLevel level;
    private Leaderboard leaderboard;
    
    @BeforeEach
    void setUp() {
        mapper = new LeaderboardMapper();
        
        // Create test user
        user = User.builder()
                .id("user1")
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hash")
                .role(UserRole.EMPLOYEE)
                .department("Engineering")
                .earnedPoints(500L)
                .availablePoints(300L)
                .build();
        
        // Create ladder level
        level = new LadderLevel(3L, "Expert", 400L);
        
        // Create leaderboard entry
        leaderboard = new Leaderboard(user, 500L, level, 5L);
    }
    
    @Test
    @DisplayName("toDTO should map Leaderboard entity to LeaderboardEntryDTO")
    void toDTO_ShouldMapLeaderboardEntityToDTO() {
        // Act
        LeaderboardEntryDTO dto = mapper.toDTO(leaderboard);
        
        // Assert
        assertNotNull(dto);
        assertEquals(user.getId(), dto.getUserId());
        assertEquals(user.getUsername(), dto.getUsername());
        assertEquals(user.getDepartment(), dto.getDepartment());
        assertEquals(leaderboard.getEarnedPoints().intValue(), dto.getEarnedPoints());
        assertEquals(level.getLevel().intValue(), dto.getCurrentLevel());
        assertEquals(level.getLabel(), dto.getLevelLabel());
        assertEquals(leaderboard.getRank().intValue(), dto.getRank());
    }
    
    @Test
    @DisplayName("toDTO should return null when input is null")
    void toDTO_ShouldReturnNullWhenInputIsNull() {
        // Act
        LeaderboardEntryDTO dto = mapper.toDTO(null);
        
        // Assert
        assertNull(dto);
    }
    
    @Test
    @DisplayName("toDTO should handle null values in Leaderboard entity")
    void toDTO_ShouldHandleNullValuesInLeaderboardEntity() {
        // Arrange
        Leaderboard nullLeaderboard = new Leaderboard();
        
        // Act
        LeaderboardEntryDTO dto = mapper.toDTO(nullLeaderboard);
        
        // Assert
        assertNotNull(dto);
        assertNull(dto.getUserId());
        assertNull(dto.getUsername());
        assertNull(dto.getDepartment());
        assertNull(dto.getEarnedPoints());
        assertNull(dto.getCurrentLevel());
        assertNull(dto.getLevelLabel());
        assertNull(dto.getRank());
    }
    
    @Test
    @DisplayName("toDTOList should map list of Leaderboard entities to list of DTOs")
    void toDTOList_ShouldMapListOfLeaderboardEntitiesToListOfDTOs() {
        // Arrange
        User user2 = User.builder()
                .id("user2")
                .username("anotheruser")
                .email("another@example.com")
                .passwordHash("hash2")
                .role(UserRole.EMPLOYEE)
                .department("Marketing")
                .earnedPoints(300L)
                .availablePoints(200L)
                .build();
        
        LadderLevel level2 = new LadderLevel(2L, "Intermediate", 200L);
        
        Leaderboard leaderboard2 = new Leaderboard(user2, 300L, level2, 10L);
        
        List<Leaderboard> leaderboards = Arrays.asList(leaderboard, leaderboard2);
        
        // Act
        List<LeaderboardEntryDTO> dtos = mapper.toDTOList(leaderboards);
        
        // Assert
        assertNotNull(dtos);
        assertEquals(2, dtos.size());
        
        // Check first DTO
        assertEquals(user.getId(), dtos.get(0).getUserId());
        assertEquals(user.getUsername(), dtos.get(0).getUsername());
        assertEquals(leaderboard.getRank().intValue(), dtos.get(0).getRank());
        
        // Check second DTO
        assertEquals(user2.getId(), dtos.get(1).getUserId());
        assertEquals(user2.getUsername(), dtos.get(1).getUsername());
        assertEquals(leaderboard2.getRank().intValue(), dtos.get(1).getRank());
    }
    
    @Test
    @DisplayName("toDTOList should return null when input is null")
    void toDTOList_ShouldReturnNullWhenInputIsNull() {
        // Act
        List<LeaderboardEntryDTO> dtos = mapper.toDTOList(null);
        
        // Assert
        assertNull(dtos);
    }
    
    @Test
    @DisplayName("toDTOPage should map Page of Leaderboard entities to Page of DTOs")
    void toDTOPage_ShouldMapPageOfLeaderboardEntitiesToPageOfDTOs() {
        // Arrange
        User user2 = User.builder()
                .id("user2")
                .username("anotheruser")
                .email("another@example.com")
                .passwordHash("hash2")
                .role(UserRole.EMPLOYEE)
                .department("Marketing")
                .earnedPoints(300L)
                .availablePoints(200L)
                .build();
        
        LadderLevel level2 = new LadderLevel(2L, "Intermediate", 200L);
        
        Leaderboard leaderboard2 = new Leaderboard(user2, 300L, level2, 10L);
        
        List<Leaderboard> leaderboards = Arrays.asList(leaderboard, leaderboard2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Leaderboard> leaderboardPage = new PageImpl<>(leaderboards, pageable, leaderboards.size());
        
        // Act
        Page<LeaderboardEntryDTO> dtoPage = mapper.toDTOPage(leaderboardPage, pageable);
        
        // Assert
        assertNotNull(dtoPage);
        assertEquals(2, dtoPage.getContent().size());
        assertEquals(2, dtoPage.getTotalElements());
        
        // Check first DTO
        assertEquals(user.getId(), dtoPage.getContent().get(0).getUserId());
        assertEquals(user.getUsername(), dtoPage.getContent().get(0).getUsername());
        assertEquals(leaderboard.getRank().intValue(), dtoPage.getContent().get(0).getRank());
        
        // Check second DTO
        assertEquals(user2.getId(), dtoPage.getContent().get(1).getUserId());
        assertEquals(user2.getUsername(), dtoPage.getContent().get(1).getUsername());
        assertEquals(leaderboard2.getRank().intValue(), dtoPage.getContent().get(1).getRank());
    }
    
    @Test
    @DisplayName("toDTOPage should return null when input is null")
    void toDTOPage_ShouldReturnNullWhenInputIsNull() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        
        // Act
        Page<LeaderboardEntryDTO> dtoPage = mapper.toDTOPage(null, pageable);
        
        // Assert
        assertNull(dtoPage);
    }
}
