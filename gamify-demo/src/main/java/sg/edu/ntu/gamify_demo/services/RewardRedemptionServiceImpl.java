package sg.edu.ntu.gamify_demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sg.edu.ntu.gamify_demo.dtos.RewardRedemptionRequestDTO;
import sg.edu.ntu.gamify_demo.models.RewardRedemption;
import sg.edu.ntu.gamify_demo.models.Rewards;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.repositories.RewardRedemptionRepository;
import sg.edu.ntu.gamify_demo.repositories.RewardRepository;
import sg.edu.ntu.gamify_demo.repositories.UserRepository;

import java.util.Optional;

@Service
public class RewardRedemptionServiceImpl {

    @Autowired
    private RewardRedemptionRepository redemptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RewardRepository rewardRepository;

    public RewardRedemption createRedemption(RewardRedemptionRequestDTO requestDTO) {
        // Fetch User and Reward
        Optional<User> userOpt = userRepository.findById(requestDTO.getUserId());
        Optional<Rewards> rewardOpt = rewardRepository.findById(requestDTO.getRewardId());

        if (userOpt.isEmpty() || rewardOpt.isEmpty()) {
            throw new RuntimeException("User or Reward not found!");
        }

        // Create new redemption
        RewardRedemption redemption = new RewardRedemption(userOpt.get(), rewardOpt.get(), requestDTO.getStatus());

        // Save to database
        return redemptionRepository.save(redemption);
    }
}