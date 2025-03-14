package sg.edu.ntu.gamify_demo.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.enums.UserRole;

/**
 * Test class for UserRepository.
 * Uses @DataJpaTest which provides database for testing JPA repositories.
 */
@DataJpaTest
@ActiveProfiles("test") // Use test profile for database testing
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        // Clear the repository before each test
        userRepository.deleteAll();

        // Create test users
        testUser1 = User.builder()
                .id("user1")
                .username("testuser1")
                .email("test1@example.com")
                .passwordHash("hashedpassword1")
                .role(UserRole.EMPLOYEE)
                .department("Engineering")
                .earnedPoints(100L)
                .availablePoints(100L)
                .build();

        testUser2 = User.builder()
                .id("user2")
                .username("testuser2")
                .email("test2@example.com")
                .passwordHash("hashedpassword2")
                .role(UserRole.MANAGER)
                .department("Marketing")
                .earnedPoints(200L)
                .availablePoints(150L)
                .build();
    }

    @Test
    @DisplayName("Test save user - Create operation")
    void testSaveUser() {
        // Save the user
        userRepository.save(testUser1);

        // Retrieve the saved user
        Optional<User> retrievedUser = userRepository.findById(testUser1.getId());

        // Verify the saved user
        assertThat(retrievedUser).isPresent();
        User savedUser = retrievedUser.get();
        assertThat(savedUser.getId()).isEqualTo(testUser1.getId());
        assertThat(savedUser.getUsername()).isEqualTo(testUser1.getUsername());
        assertThat(savedUser.getEmail()).isEqualTo(testUser1.getEmail());
        assertThat(savedUser.getPasswordHash()).isEqualTo(testUser1.getPasswordHash());
        assertThat(savedUser.getRole()).isEqualTo(testUser1.getRole());
        assertThat(savedUser.getDepartment()).isEqualTo(testUser1.getDepartment());
        assertThat(savedUser.getEarnedPoints()).isEqualTo(testUser1.getEarnedPoints());
        assertThat(savedUser.getAvailablePoints()).isEqualTo(testUser1.getAvailablePoints());
    }

    @Test
    @DisplayName("Test find user by ID - Read operation")
    void testFindUserById() {
        // Save the user
        userRepository.save(testUser1);

        // Find the user by ID
        Optional<User> foundUser = userRepository.findById(testUser1.getId());

        // Verify the found user
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo(testUser1.getUsername());
        assertThat(foundUser.get().getEmail()).isEqualTo(testUser1.getEmail());
    }

    @Test
    @DisplayName("Test find user by username - Read operation")
    void testFindUserByUsername() {
        // Save the user
        userRepository.save(testUser1);

        // Find the user by username
        Optional<User> foundUser = userRepository.findByUsername(testUser1.getUsername());

        // Verify the found user
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(testUser1.getId());
        assertThat(foundUser.get().getEmail()).isEqualTo(testUser1.getEmail());
    }

    @Test
    @DisplayName("Test find user by email - Read operation")
    void testFindUserByEmail() {
        // Save the user
        userRepository.save(testUser1);

        // Find the user by email
        Optional<User> foundUser = userRepository.findByEmail(testUser1.getEmail());

        // Verify the found user
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(testUser1.getId());
        assertThat(foundUser.get().getUsername()).isEqualTo(testUser1.getUsername());
    }

    @Test
    @DisplayName("Test update user - Update operation")
    void testUpdateUser() {
        // Save the user
        userRepository.save(testUser1);

        // Retrieve the saved user
        Optional<User> retrievedUserOpt = userRepository.findById(testUser1.getId());
        assertThat(retrievedUserOpt).isPresent();
        User savedUser = retrievedUserOpt.get();

        // Update the user
        savedUser.setDepartment("Research");
        savedUser.setEarnedPoints(150L);
        savedUser.setAvailablePoints(120L);
        userRepository.save(savedUser);

        // Retrieve the updated user
        Optional<User> updatedUserOpt = userRepository.findById(testUser1.getId());
        assertThat(updatedUserOpt).isPresent();
        User updatedUser = updatedUserOpt.get();

        // Verify the updated user
        assertThat(updatedUser.getId()).isEqualTo(testUser1.getId());
        assertThat(updatedUser.getDepartment()).isEqualTo("Research");
        assertThat(updatedUser.getEarnedPoints()).isEqualTo(150);
        assertThat(updatedUser.getAvailablePoints()).isEqualTo(120);
    }

    @Test
    @DisplayName("Test delete user - Delete operation")
    void testDeleteUser() {
        // Save the user
        userRepository.save(testUser1);

        // Delete the user
        userRepository.deleteById(testUser1.getId());

        // Verify the user is deleted
        Optional<User> deletedUser = userRepository.findById(testUser1.getId());
        assertThat(deletedUser).isEmpty();
    }

    @Test
    @DisplayName("Test find all users - Read operation")
    void testFindAllUsers() {
        // Save multiple users
        userRepository.save(testUser1);
        userRepository.save(testUser2);

        // Find all users
        List<User> users = userRepository.findAll();

        // Verify the users
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getUsername)
                .containsExactlyInAnyOrder(testUser1.getUsername(), testUser2.getUsername());
    }

    @Test
    @DisplayName("Test find all users sorted - Read operation")
    void testFindAllUsersSorted() {
        // Save multiple users
        userRepository.save(testUser1);
        userRepository.save(testUser2);

        // Find all users sorted by ID
        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

        // Verify the users are sorted
        assertThat(users).hasSize(2);
        assertThat(users.get(0).getId()).isEqualTo(testUser1.getId());
        assertThat(users.get(1).getId()).isEqualTo(testUser2.getId());
    }

    @Test
    @DisplayName("Test exists by username - Read operation")
    void testExistsByUsername() {
        // Save the user
        userRepository.save(testUser1);

        // Check if user exists by username
        boolean exists = userRepository.existsByUsername(testUser1.getUsername());
        boolean notExists = userRepository.existsByUsername("nonexistentuser");

        // Verify the results
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Test exists by email - Read operation")
    void testExistsByEmail() {
        // Save the user
        userRepository.save(testUser1);

        // Check if user exists by email
        boolean exists = userRepository.existsByEmail(testUser1.getEmail());
        boolean notExists = userRepository.existsByEmail("nonexistent@example.com");

        // Verify the results
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}
