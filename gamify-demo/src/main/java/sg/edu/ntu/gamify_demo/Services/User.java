package sg.edu.ntu.gamify_demo.Services;
import java.time.LocalDateTime;

/**
 * The User class represents a user in the system.
 * It stores the user's details including personal information and timestamps for creation and updates.
 */
public class User 
{
    //Instance variables
    private int userId, points;
    private String userName, email, passwordHash, role, department;
    private LocalDateTime createdAt, updatedAt;
    
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
    public User(int userId, int points, String userName, String email, String passwordHash, String role, String department, LocalDateTime createdAt, LocalDateTime updatedAt) 
    {
        this.userId = userId;
        this.points = points;
        this.userName = userName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.department = department;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    /**
     * Gets the unique identifier of the user.
     * 
     * @return The user ID.
     */
    public int getUserId() 
    {
        return userId;
    }

    /**
     * Sets the unique identifier of the user.
     * 
     * @param userId The new user ID.
     */
    public void setUserId(int userId) 
    {
        this.userId = userId;
    }

    /**
     * Gets the points associated with the user.
     * 
     * @return The points of the user.
     */
    public int getPoints() 
    {
        return points;
    }

    /**
     * Sets the points associated with the user.
     * 
     * @param points The new points value.
     */
    public void setPoints(int points) 
    {
        this.points = points;
    }

    /**
     * Gets the username of the user.
     * 
     * @return The user's username.
     */
    public String getUserName() 
    {
        return userName;
    }

    /**
     * Sets the username of the user.
     * 
     * @param userName The new username for the user.
     */
    public void setUsername(String userName) 
    {
        this.userName = userName;
    }

    /**
     * Gets the email address of the user.
     * 
     * @return The user's email address.
     */
    public String getEmail() 
    {
        return email;
    }

    /**
     * Sets the email address of the user.
     * 
     * @param email The new email address for the user.
     */
    public void setEmail(String email) 
    {
        this.email = email;
    }

    /**
     * Gets the hashed password of the user.
     * 
     * @return The hashed password of the user.
     */
    public String getPasswordHash() 
    {
        return passwordHash;
    }

    /**
     * Sets the hashed password of the user.
     * 
     * @param passwordHash The new hashed password for the user.
     */
    public void setPasswordHash(String passwordHash) 
    {
        this.passwordHash = passwordHash;
    }

    /**
     * Gets the role of the user (e.g., admin, member).
     * 
     * @return The user's role.
     */
    public String getRole() 
    {
        return role;
    }

    /**
     * Sets the role of the user.
     * 
     * @param role The new role for the user.
     */
    public void setRole(String role) 
    {
        this.role = role;
    }

    /**
     * Gets the department of the user.
     * 
     * @return The department the user belongs to.
     */
    public String getDepartment() 
    {
        return department;
    }

    /**
     * Sets the department of the user.
     * 
     * @param department The new department for the user.
     */
    public void setDepartment(String department) 
    {
        this.department = department;
    }

    /**
     * Gets the creation timestamp of the user.
     * 
     * @return The timestamp when the user was created.
     */
    public LocalDateTime getCreatedAt() 
    {
        return createdAt;
    }

    /**
     * Sets the creation timestamp of the user.
     * 
     * @param createdAt The new timestamp for when the user was created.
     */
    public void setCreatedAt(LocalDateTime createdAt) 
    {
        this.createdAt = createdAt;
    }

    /**
     * Gets the last updated timestamp of the user.
     * 
     * @return The timestamp of the last update to the user's details.
     */
    public LocalDateTime getUpdatedAt() 
    {
        return updatedAt;
    }

    /**
     * Sets the last updated timestamp of the user.
     * 
     * @param updatedAt The new timestamp for the last update to the user's details.
     */
    public void setUpdatedAt(LocalDateTime updatedAt) 
    {
        this.updatedAt = updatedAt;
    }
}