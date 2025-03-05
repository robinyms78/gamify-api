// Java class for User
// User.java

package sg.edu.ntu.gamify_demo.models;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The User class represents a user in the system.
 * It stores the user's details including personal information and timestamps for creation and updates.
 */

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User 
{
    //Instance variables
    private int points;
    protected String userId = UUID.randomUUID().toString(); 
    private String userName;
    private String email;
    private String passwordHash;
    private String role;
    private String department;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Constructs a User object with the provided details.
     * 
     * @param userId The unique identifier for the user.
     * @param points The points associated with the user.
     * @param userName The name of the user.
     * @param email The email address of the user.
     * @param passwordHash The hashed password of the user.
     * @param role The role of the user (e.g., admin, member).
     * @param department The department the user belongs to.
     * @param createdAt The timestamp of when the user was created.
     * @param updatedAt The timestamp of when the user was last updated.
     */
}