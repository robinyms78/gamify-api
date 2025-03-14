package sg.edu.ntu.gamify_demo.tests;

import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import sg.edu.ntu.gamify_demo.models.Rewards;
import sg.edu.ntu.gamify_demo.repositories.RewardRepository;
import sg.edu.ntu.gamify_demo.repositories.RewardRedemptionRepository;
import java.time.ZonedDateTime;

@Component
public class DataLoader {
    private RewardRepository rewardRepository;
    private RewardRedemptionRepository redemptionRepository;

    // Constructor injection
    public DataLoader(RewardRepository rewardRepository, RewardRedemptionRepository redemptionRepository) {
        this.rewardRepository = rewardRepository;
        this.redemptionRepository = redemptionRepository;
    }

    @PostConstruct
    public void loadData() {
        // clear the redemptions first to avoid foreign key constraint violations
        redemptionRepository.deleteAll();
        
        // then clear the rewards
        rewardRepository.deleteAll();

        // load data here
        rewardRepository.save(Rewards.builder().name("iphone").description("electronics").costInPoints(1000L).available(true).createdAt(ZonedDateTime.now()).updatedAt(ZonedDateTime.now()).build());
        rewardRepository.save(Rewards.builder().name("laptop").description("electronics").costInPoints(3000L).available(false).createdAt(ZonedDateTime.now()).updatedAt(ZonedDateTime.now()).build());
        rewardRepository.save(Rewards.builder().name("NTUC-voucher").description("grocery").costInPoints(100L).available(true).createdAt(ZonedDateTime.now()).updatedAt(ZonedDateTime.now()).build());
        rewardRepository.save(Rewards.builder().name("air-ticket").description("holiday").costInPoints(5000L).available(false).createdAt(ZonedDateTime.now()).updatedAt(ZonedDateTime.now()).build());
    }
}
