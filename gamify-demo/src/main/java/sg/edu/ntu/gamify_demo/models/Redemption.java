// Java class for Redemption
// Redemption.java

package sg.edu.ntu.gamify_demo.models;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The Redemption class represents a class which check the reward redemption status the user in the system.
 * It stores the redemption details including redemptionId and timestamps for creation and updates.
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "redemptions")
public class Redemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @JsonIgnoreProperties("redemption")
    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", referencedColumnName = "id")
    private Employee employee;

    @JsonIgnoreProperties("redemption")
    @ManyToOne(optional = false)
    @JoinColumn(name = "reward_id", referencedColumnName = "id")
    private Reward reward;

    /**
     * Constructs a Redemption object with the provided details.
     * @param id The unique identifier of the employee.
     * @param redemption id The unique identifier of the redemption.
     * @param reward_id The unique identifier of the reward.
     * @param status The redemption status.
     * @param createdAt The timestamp of when the redemption was created.
     * @param updatedAt The timestamp of when the redemption was last updated.
     */
}