package sg.edu.ntu.gamify_demo.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RedemptionResult {
    private boolean success;
    private String message;
}
