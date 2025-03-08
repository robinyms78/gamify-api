package sg.edu.ntu.gamify_demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sg.edu.ntu.gamify_demo.models.Achievement;

/**
 * Repository for Achievement entities.
 * Provides CRUD operations and custom queries for achievements.
 */
@Repository
public interface AchievementRepository extends JpaRepository<Achievement, String> {
    // Find achievement by name
    Achievement findByName(String name);
}
