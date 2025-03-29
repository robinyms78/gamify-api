package sg.edu.ntu.gamify_demo.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import sg.edu.ntu.gamify_demo.models.User;

public interface UserRepository extends JpaRepository<User, String> {
    // Custom query methods to check if username or email already exists
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    // Method to fetch all users sorted by ID in ascending order
    @Override
    @NonNull
    List<User> findAll(@NonNull Sort sort);
}
