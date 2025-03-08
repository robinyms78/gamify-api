package sg.edu.ntu.gamify_demo.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import sg.edu.ntu.gamify_demo.exceptions.UserValidationException;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.enums.UserRole;

/**
 * Test class for UserValidator.
 * Tests validation logic for User objects.
 */
public class UserValidatorTest {

    private UserValidator userValidator;
    private User validUser;

    @BeforeEach
    void setUp() {
        userValidator = new UserValidator();
        
        // Create a valid user for testing
        validUser = User.builder()
                .id("user1")
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .role(UserRole.EMPLOYEE)
                .department("Engineering")
                .earnedPoints(100)
                .availablePoints(100)
                .build();
    }

    @Test
    @DisplayName("Test validation with valid user")
    void testValidateUser_ValidUser() {
        // Validate a valid user
        assertDoesNotThrow(() -> userValidator.validateUser(validUser));
    }

    @Test
    @DisplayName("Test validation with null username")
    void testValidateUser_NullUsername() {
        // Create user with null username
        User invalidUser = User.builder()
                .id("user1")
                .username(null)
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .role(UserRole.EMPLOYEE)
                .build();

        // Validate and expect exception
        UserValidationException exception = assertThrows(UserValidationException.class, 
                () -> userValidator.validateUser(invalidUser));
        
        // Verify exception message
        assertTrue(exception.getMessage().contains("Username cannot be empty"));
    }

    @Test
    @DisplayName("Test validation with empty username")
    void testValidateUser_EmptyUsername() {
        // Create user with empty username
        User invalidUser = User.builder()
                .id("user1")
                .username("")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .role(UserRole.EMPLOYEE)
                .build();

        // Validate and expect exception
        UserValidationException exception = assertThrows(UserValidationException.class, 
                () -> userValidator.validateUser(invalidUser));
        
        // Verify exception message
        assertTrue(exception.getMessage().contains("Username cannot be empty"));
    }

    @Test
    @DisplayName("Test validation with blank username")
    void testValidateUser_BlankUsername() {
        // Create user with blank username (only whitespace)
        User invalidUser = User.builder()
                .id("user1")
                .username("   ")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .role(UserRole.EMPLOYEE)
                .build();

        // Validate and expect exception
        UserValidationException exception = assertThrows(UserValidationException.class, 
                () -> userValidator.validateUser(invalidUser));
        
        // Verify exception message
        assertTrue(exception.getMessage().contains("Username cannot be empty"));
    }

    @Test
    @DisplayName("Test validation with null email")
    void testValidateUser_NullEmail() {
        // Create user with null email
        User invalidUser = User.builder()
                .id("user1")
                .username("testuser")
                .email(null)
                .passwordHash("hashedpassword")
                .role(UserRole.EMPLOYEE)
                .build();

        // Validate and expect exception
        UserValidationException exception = assertThrows(UserValidationException.class, 
                () -> userValidator.validateUser(invalidUser));
        
        // Verify exception message
        assertTrue(exception.getMessage().contains("Email cannot be empty"));
    }

    @Test
    @DisplayName("Test validation with empty email")
    void testValidateUser_EmptyEmail() {
        // Create user with empty email
        User invalidUser = User.builder()
                .id("user1")
                .username("testuser")
                .email("")
                .passwordHash("hashedpassword")
                .role(UserRole.EMPLOYEE)
                .build();

        // Validate and expect exception
        UserValidationException exception = assertThrows(UserValidationException.class, 
                () -> userValidator.validateUser(invalidUser));
        
        // Verify exception message
        assertTrue(exception.getMessage().contains("Email cannot be empty"));
    }

    @Test
    @DisplayName("Test validation with blank email")
    void testValidateUser_BlankEmail() {
        // Create user with blank email (only whitespace)
        User invalidUser = User.builder()
                .id("user1")
                .username("testuser")
                .email("   ")
                .passwordHash("hashedpassword")
                .role(UserRole.EMPLOYEE)
                .build();

        // Validate and expect exception
        UserValidationException exception = assertThrows(UserValidationException.class, 
                () -> userValidator.validateUser(invalidUser));
        
        // Verify exception message
        assertTrue(exception.getMessage().contains("Email cannot be empty"));
    }

    @Test
    @DisplayName("Test validation with null password hash")
    void testValidateUser_NullPasswordHash() {
        // Create user with null password hash
        User invalidUser = User.builder()
                .id("user1")
                .username("testuser")
                .email("test@example.com")
                .passwordHash(null)
                .role(UserRole.EMPLOYEE)
                .build();

        // Validate and expect exception
        UserValidationException exception = assertThrows(UserValidationException.class, 
                () -> userValidator.validateUser(invalidUser));
        
        // Verify exception message
        assertTrue(exception.getMessage().contains("Password hash cannot be empty"));
    }

    @Test
    @DisplayName("Test validation with empty password hash")
    void testValidateUser_EmptyPasswordHash() {
        // Create user with empty password hash
        User invalidUser = User.builder()
                .id("user1")
                .username("testuser")
                .email("test@example.com")
                .passwordHash("")
                .role(UserRole.EMPLOYEE)
                .build();

        // Validate and expect exception
        UserValidationException exception = assertThrows(UserValidationException.class, 
                () -> userValidator.validateUser(invalidUser));
        
        // Verify exception message
        assertTrue(exception.getMessage().contains("Password hash cannot be empty"));
    }

    @Test
    @DisplayName("Test validation with blank password hash")
    void testValidateUser_BlankPasswordHash() {
        // Create user with blank password hash (only whitespace)
        User invalidUser = User.builder()
                .id("user1")
                .username("testuser")
                .email("test@example.com")
                .passwordHash("   ")
                .role(UserRole.EMPLOYEE)
                .build();

        // Validate and expect exception
        UserValidationException exception = assertThrows(UserValidationException.class, 
                () -> userValidator.validateUser(invalidUser));
        
        // Verify exception message
        assertTrue(exception.getMessage().contains("Password hash cannot be empty"));
    }

    @Test
    @DisplayName("Test validation with null role")
    void testValidateUser_NullRole() {
        // Create user with null role
        User invalidUser = User.builder()
                .id("user1")
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .role(null)
                .build();

        // Validate and expect exception
        UserValidationException exception = assertThrows(UserValidationException.class, 
                () -> userValidator.validateUser(invalidUser));
        
        // Verify exception message
        assertTrue(exception.getMessage().contains("Role cannot be null"));
    }
}
