package sg.edu.ntu.gamify_demo.dtos;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for returning the result of a reward redemption operation.
 * Contains information about the success/failure of the operation,
 * as well as additional details like the updated points balance.
 */
@Getter 
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RedemptionResult {
    private boolean success;
    private String message;
    private Long updatedPointsBalance;
    private String redemptionId;
    private ZonedDateTime timestamp;
}
