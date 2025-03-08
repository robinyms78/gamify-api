package sg.edu.ntu.gamify_demo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sg.edu.ntu.gamify_demo.models.Achievement;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.UserAchievement;
import sg.edu.ntu.gamify_demo.models.UserAchievementId;

/**
 * Repository for UserAchievement entities.
 * Provides CRUD operations and custom queries for user achievements.
 */
@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, UserAchievementId> {
    // Find all achievements for a specific user
    List<UserAchievement> findByUser(User user);
    
    // Find all users who have earned a specific achievement
    List<UserAchievement> findByAchievement(Achievement achievement);
    
    // Check if a user has a specific achievement
    boolean existsByUserAndAchievement(User user, Achievement achievement);
    
    // Count the number of achievements a user has earned
    long countByUser(User user);
}
