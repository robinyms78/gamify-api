package sg.edu.ntu.gamify_demo.schedulers;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import sg.edu.ntu.gamify_demo.interfaces.LeaderboardService;

@ExtendWith(MockitoExtension.class)
class LeaderboardSchedulerTest {

    @Mock
    private LeaderboardService leaderboardService;
    
    @InjectMocks
    private LeaderboardScheduler leaderboardScheduler;
    
    @BeforeEach
    void setUp() {
        // No additional setup needed
    }
    
    @Test
    @DisplayName("Refresh all rankings should call calculateRanks on service")
    void refreshAllRankings_ShouldCallCalculateRanksOnService() {
        // Arrange
        when(leaderboardService.calculateRanks()).thenReturn(10);
        
        // Act
        leaderboardScheduler.refreshAllRankings();
        
        // Assert
        verify(leaderboardService, times(1)).calculateRanks();
    }
    
    @Test
    @DisplayName("Refresh all rankings should handle exceptions gracefully")
    void refreshAllRankings_ShouldHandleExceptionsGracefully() {
        // Arrange
        when(leaderboardService.calculateRanks()).thenThrow(new RuntimeException("Test exception"));
        
        // Act - This should not throw an exception
        leaderboardScheduler.refreshAllRankings();
        
        // Assert
        verify(leaderboardService, times(1)).calculateRanks();
        // No assertion for logging since we can't easily mock the static logger
    }
}
