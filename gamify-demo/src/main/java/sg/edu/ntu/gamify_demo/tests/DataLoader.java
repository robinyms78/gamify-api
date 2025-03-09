package sg.edu.ntu.gamify_demo.tests;

import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import sg.edu.ntu.gamify_demo.models.Reward;
import sg.edu.ntu.gamify_demo.repositories.RewardRepository;
import java.time.LocalDateTime;

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
        rewardRepository.save(Reward.builder().name("iphone").description("electronics").costInPoints(1000).available(true).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build());
        rewardRepository.save(Reward.builder().name("laptop").description("electronics").costInPoints(3000).available(false).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build());
        rewardRepository.save(Reward.builder().name("NTUC-voucher").description("grocery").costInPoints(100).available(true).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build());
        rewardRepository.save(Reward.builder().name("air-ticket").description("holiday").costInPoints(5000).available(false).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build());
    }
}