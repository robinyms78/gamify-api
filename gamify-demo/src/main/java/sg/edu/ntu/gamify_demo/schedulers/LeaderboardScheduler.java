package sg.edu.ntu.gamify_demo.schedulers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import sg.edu.ntu.gamify_demo.interfaces.LeaderboardService;

/**
 * Scheduler for periodic leaderboard operations.
 * Handles tasks like refreshing rankings at regular intervals.
 */
@Component
public class LeaderboardScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(LeaderboardScheduler.class);
    
    private final LeaderboardService leaderboardService;
    
    @Autowired
    public LeaderboardScheduler(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }
    
    /**
     * Refreshes all rankings on the leaderboard.
     * Runs every hour by default, configurable via application properties.
     */
    @Scheduled(fixedRateString = "${leaderboard.refresh.rate:3600000}")
    public void refreshAllRankings() {
        logger.info("Starting scheduled leaderboard rank refresh");
        
        try {
            int updatedCount = leaderboardService.calculateRanks();
            logger.info("Leaderboard rank refresh completed. Updated {} entries", updatedCount);
        } catch (Exception e) {
            logger.error("Error during leaderboard rank refresh: {}", e.getMessage(), e);
        }
    }
}
