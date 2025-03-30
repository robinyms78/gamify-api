package sg.edu.ntu.gamify_demo.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import sg.edu.ntu.gamify_demo.exceptions.UserNotFoundException;
import sg.edu.ntu.gamify_demo.exceptions.UserValidationException;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.enums.UserRole;
import sg.edu.ntu.gamify_demo.repositories.UserRepository;

/**
 * Test class for UserServiceImpl.
 * Uses Mockito to mock dependencies.
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserValidator userValidator;
    
    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
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
    @DisplayName("Test create user - Success scenario")
    void testCreateUser_Success() throws UserValidationException {
        // Mock validator to do nothing (validation passes)
        doNothing().when(userValidator).validateUser(any(User.class));
        
        // Mock encoder to return a hashed password
        when(encoder.encode(any(String.class))).thenReturn("encoded_password");
        
        // Mock repository to return the saved user
        when(userRepository.save(any(User.class))).thenReturn(testUser1);

        // Call the service method
        User createdUser = userService.createUser(testUser1);

        // Verify the result
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isEqualTo(testUser1.getId());
        assertThat(createdUser.getUsername()).isEqualTo(testUser1.getUsername());
        
        // Verify the validator and repository were called
        verify(userValidator, times(1)).validateUser(testUser1);
        verify(userRepository, times(1)).save(testUser1);
    }

    @Test
    @DisplayName("Test create user - Validation failure")
    void testCreateUser_ValidationFailure() throws UserValidationException {
        // Mock validator to throw exception
        UserValidationException exception = new UserValidationException("Validation failed");
        
        // Create invalid user
        User invalidUser = User.builder().build(); // Empty user
        
        // Mock the validator to throw exception for invalid user
        doThrow(exception).when(userValidator).validateUser(invalidUser); // Specific behavior

        // Call the service method and expect exception
        assertThrows(UserValidationException.class, () -> {
            userService.createUser(invalidUser);
        });
        
        // Verify the repository was not called
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Test get user by ID - Success scenario")
    void testGetUserById_Success() {
        // Mock repository to return the user
        when(userRepository.findById(testUser1.getId())).thenReturn(Optional.of(testUser1));

        // Call the service method
        User foundUser = userService.getUserById(testUser1.getId());

        // Verify the result
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(testUser1.getId());
        assertThat(foundUser.getUsername()).isEqualTo(testUser1.getUsername());
        
        // Verify the repository was called
        verify(userRepository, times(1)).findById(testUser1.getId());
    }

    @Test
    @DisplayName("Test get user by ID - User not found")
    void testGetUserById_UserNotFound() {
        // Mock repository to return empty optional
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        // Call the service method and expect exception
        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById("nonexistentid");
        });
        
        // Verify the repository was called
        verify(userRepository, times(1)).findById("nonexistentid");
    }

    @Test
    @DisplayName("Test get all users")
    void testGetAllUsers() {
        // Mock repository to return list of users
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser1, testUser2));

        // Call the service method
        List<User> users = userService.getAllUsers();

        // Verify the result
        assertThat(users).isNotNull();
        assertThat(users).hasSize(2);
        assertThat(users).contains(testUser1, testUser2);
        
        // Verify the repository was called
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Test update user - Success scenario")
    void testUpdateUser_Success() throws UserValidationException {
        // Mock validator to do nothing (validation passes)
        doNothing().when(userValidator).validateUser(any(User.class));
        
        // Mock repository to check if user exists
        when(userRepository.existsById(testUser1.getId())).thenReturn(true);
        
        // Mock repository to return the updated user
        when(userRepository.save(any(User.class))).thenReturn(testUser1);

        // Create updated user
        User updatedUser = User.builder()
                .username(testUser1.getUsername())
                .email(testUser1.getEmail())
                .passwordHash(testUser1.getPasswordHash())
                .role(testUser1.getRole())
                .department("Research") // Updated department
                .earnedPoints(150L) // Updated points
                .availablePoints(120L) // Updated points to match the type
                .build();

        // Call the service method
        User result = userService.updateUser(testUser1.getId(), updatedUser);

        // Verify the result
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUser1.getId());
        
        // Verify the validator and repository were called
        verify(userValidator, times(1)).validateUser(updatedUser);
        verify(userRepository, times(1)).existsById(testUser1.getId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Test update user - User not found")
    void testUpdateUser_UserNotFound() throws UserValidationException {
        // Mock validator to do nothing (validation passes)
        doNothing().when(userValidator).validateUser(any(User.class));
        
        // Mock repository to check if user exists
        when(userRepository.existsById(anyString())).thenReturn(false);

        // Call the service method and expect exception
        assertThrows(UserNotFoundException.class, () -> {
            userService.updateUser("nonexistentid", testUser1);
        });
        
        // Verify the repository was called but save was not
        verify(userRepository, times(1)).existsById("nonexistentid");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Test update user - Validation failure")
    void testUpdateUser_ValidationFailure() throws UserValidationException {
        // Create invalid user
        User invalidUser = User.builder().build(); // Empty user
        
        // Mock validator to throw exception
        UserValidationException exception = new UserValidationException("Validation failed");
        doThrow(exception).when(userValidator).validateUser(invalidUser); // Specific behavior
        
        // Call the service method and expect exception
        assertThrows(UserValidationException.class, () -> {
            userService.updateUser(testUser1.getId(), invalidUser);
        });
        
        // Verify the repository was not called
        verify(userRepository, never()).existsById(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Test delete user - Success scenario")
    void testDeleteUser_Success() {
        // Mock repository to check if user exists
        when(userRepository.existsById(testUser1.getId())).thenReturn(true);
        
        // Mock repository to do nothing when deleteById is called
        doNothing().when(userRepository).deleteById(testUser1.getId());

        // Call the service method
        userService.deleteUser(testUser1.getId());
        
        // Verify the repository was called
        verify(userRepository, times(1)).existsById(testUser1.getId());
        verify(userRepository, times(1)).deleteById(testUser1.getId());
    }

    @Test
    @DisplayName("Test delete user - User not found")
    void testDeleteUser_UserNotFound() {
        // Mock repository to check if user exists
        when(userRepository.existsById(anyString())).thenReturn(false);

        // Call the service method and expect exception
        assertThrows(UserNotFoundException.class, () -> {
            userService.deleteUser("nonexistentid");
        });
        
        // Verify the repository was called but deleteById was not
        verify(userRepository, times(1)).existsById("nonexistentid");
        verify(userRepository, never()).deleteById(anyString());
    }
}
