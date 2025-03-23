package sg.edu.ntu.gamify_demo.tests;

import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import sg.edu.ntu.gamify_demo.models.Rewards;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.enums.UserRole;
import sg.edu.ntu.gamify_demo.repositories.UserRepository;
import sg.edu.ntu.gamify_demo.repositories.RewardRepository;
import sg.edu.ntu.gamify_demo.repositories.RewardRedemptionRepository;
import java.time.ZonedDateTime;

@Component
public class DataLoader {
    private UserRepository userRepository;
    private RewardRepository rewardRepository;
    private RewardRedemptionRepository redemptionRepository;

    // Constructor injection
    public DataLoader(UserRepository userRepository, RewardRepository rewardRepository, RewardRedemptionRepository redemptionRepository) {
        this.userRepository = userRepository;
        this.rewardRepository = rewardRepository;
        this.redemptionRepository = redemptionRepository;
    }

    @PostConstruct
    public void loadData() {
        // clear the repository
        userRepository.deleteAll();
        redemptionRepository.deleteAll();
        rewardRepository.deleteAll();

        // load user repository data
        userRepository.save(User.builder().username("Tom").email("tom@gmail.com").department("IT").role(UserRole.EMPLOYEE).passwordHash("12345678").earnedPoints(100L).availablePoints(500L).build());
        userRepository.save(User.builder().username("Jack").email("jack@gmail.com").department("IT").role(UserRole.EMPLOYEE).passwordHash("12345677").earnedPoints(100L).availablePoints(0L).build());
        userRepository.save(User.builder().username("Susan").email("susan@gmail.com").department("IT").role(UserRole.HR_ADMIN).passwordHash("12345676").earnedPoints(0L).availablePoints(500L).build());
        userRepository.save(User.builder().username("Terry").email("terry@gmail.com").department("IT").role(UserRole.MANAGER).passwordHash("12345675").earnedPoints(300L).availablePoints(500L).build());
        userRepository.save(User.builder().username("Emily").email("emily@gmail.com").department("IT").role(UserRole.EMPLOYEE).passwordHash("12345674").earnedPoints(400L).availablePoints(500L).build());

        // load reward repository data
        rewardRepository.save(Rewards.builder().name("iphone").description("electronics").costInPoints(1000L).available(true).createdAt(ZonedDateTime.now()).updatedAt(ZonedDateTime.now()).build());
        rewardRepository.save(Rewards.builder().name("laptop").description("electronics").costInPoints(3000L).available(false).createdAt(ZonedDateTime.now()).updatedAt(ZonedDateTime.now()).build());
        rewardRepository.save(Rewards.builder().name("NTUC-voucher").description("grocery").costInPoints(100L).available(true).createdAt(ZonedDateTime.now()).updatedAt(ZonedDateTime.now()).build());
        rewardRepository.save(Rewards.builder().name("air-ticket").description("holiday").costInPoints(5000L).available(false).createdAt(ZonedDateTime.now()).updatedAt(ZonedDateTime.now()).build());
    }
}
