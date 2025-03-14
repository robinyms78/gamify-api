package sg.edu.ntu.gamify_demo.tests;

import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import sg.edu.ntu.gamify_demo.models.Reward;
import sg.edu.ntu.gamify_demo.repositories.RewardRepository;
import java.time.ZonedDateTime;

@Component
public class DataLoader {
    private RewardRepository rewardRepository;

    // Constructor injection
    public DataLoader(RewardRepository rewardRepository) {
        this.rewardRepository = rewardRepository;
    }

    @PostConstruct
    public void loadData() {
        // clear the database first
        rewardRepository.deleteAll();

        // load data here
        rewardRepository.save(Reward.builder().name("iphone").description("electronics").costInPoints(1000L).available(true).createdAt(ZonedDateTime.now()).updatedAt(ZonedDateTime.now()).build());
        rewardRepository.save(Reward.builder().name("laptop").description("electronics").costInPoints(3000L).available(false).createdAt(ZonedDateTime.now()).updatedAt(ZonedDateTime.now()).build());
        rewardRepository.save(Reward.builder().name("NTUC-voucher").description("grocery").costInPoints(100L).available(true).createdAt(ZonedDateTime.now()).updatedAt(ZonedDateTime.now()).build());
        rewardRepository.save(Reward.builder().name("air-ticket").description("holiday").costInPoints(5000L).available(false).createdAt(ZonedDateTime.now()).updatedAt(ZonedDateTime.now()).build());
    }
}
