package sg.edu.ntu.gamify_demo.dtos;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Achievement information.
 * Used to transfer achievement data between layers and to the client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardsDTO {
    private String id;
    private String name;
    private String description;
    private int costInPoints;
    private boolean available;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}