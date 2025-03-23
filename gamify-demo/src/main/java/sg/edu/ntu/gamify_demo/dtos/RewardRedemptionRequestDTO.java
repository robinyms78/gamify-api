package sg.edu.ntu.gamify_demo.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardRedemptionRequestDTO {
    private String userId;
    private String rewardId;
    private String status;
}