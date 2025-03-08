package sg.edu.ntu.gamify_demo.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.enums.UserRole;
import sg.edu.ntu.gamify_demo.repositories.UserRepository;

/**
 * Integration test for User CRUD operations.
 * Tests the full flow from controller to repository.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Use test profile to use H2 database
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Clear the repository before each test
        userRepository.deleteAll();

        // Create test user
        testUser = User.builder()
                .id("integration-test-user")
                .username("integrationtestuser")
                .email("integration@example.com")
                .passwordHash("hashedpassword")
                .role(UserRole.EMPLOYEE)
                .department("Testing")
                .earnedPoints(100)
                .availablePoints(100)
                .build();
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Integration test - Create user")
    void testCreateUser() throws Exception {
        // Perform POST request to create user
        MvcResult result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.username").value(testUser.getUsername()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                .andReturn();

        // Verify user is saved in repository
        Optional<User> savedUser = userRepository.findById(testUser.getId());
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getUsername()).isEqualTo(testUser.getUsername());
        assertThat(savedUser.get().getEmail()).isEqualTo(testUser.getEmail());
    }

    @Test
    @DisplayName("Integration test - Get user by ID")
    void testGetUserById() throws Exception {
        // Save user to repository
        userRepository.save(testUser);

        // Perform GET request to retrieve user
        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.username").value(testUser.getUsername()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()));
    }

    @Test
    @DisplayName("Integration test - Get user by ID - Not found")
    void testGetUserById_NotFound() throws Exception {
        // Perform GET request for non-existent user
        mockMvc.perform(get("/api/users/{id}", "nonexistentid"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Integration test - Get all users")
    void testGetAllUsers() throws Exception {
        // Save multiple users to repository
        User anotherUser = User.builder()
                .id("another-test-user")
                .username("anothertestuser")
                .email("another@example.com")
                .passwordHash("hashedpassword")
                .role(UserRole.MANAGER)
                .department("Management")
                .earnedPoints(200)
                .availablePoints(150)
                .build();
        
        userRepository.save(testUser);
        userRepository.save(anotherUser);

        // Perform GET request to retrieve all users
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        // Verify users in repository
        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(2);
    }

    @Test
    @DisplayName("Integration test - Update user")
    void testUpdateUser() throws Exception {
        // Save user to repository
        userRepository.save(testUser);

        // Create updated user
        User updatedUser = User.builder()
                .id(testUser.getId()) // Same ID
                .username(testUser.getUsername()) // Same username
                .email(testUser.getEmail()) // Same email
                .passwordHash(testUser.getPasswordHash()) // Same password hash
                .role(testUser.getRole()) // Same role
                .department("Updated Department") // Updated department
                .earnedPoints(150) // Updated points
                .availablePoints(120) // Updated points
                .build();

        // Perform PUT request to update user
        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.department").value("Updated Department"))
                .andExpect(jsonPath("$.earnedPoints").value(150))
                .andExpect(jsonPath("$.availablePoints").value(120));

        // Verify user is updated in repository
        Optional<User> savedUser = userRepository.findById(testUser.getId());
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getDepartment()).isEqualTo("Updated Department");
        assertThat(savedUser.get().getEarnedPoints()).isEqualTo(150);
        assertThat(savedUser.get().getAvailablePoints()).isEqualTo(120);
    }

    @Test
    @DisplayName("Integration test - Update user - Not found")
    void testUpdateUser_NotFound() throws Exception {
        // Perform PUT request for non-existent user
        mockMvc.perform(put("/api/users/{id}", "nonexistentid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Integration test - Delete user")
    void testDeleteUser() throws Exception {
        // Save user to repository
        userRepository.save(testUser);

        // Perform DELETE request to delete user
        mockMvc.perform(delete("/api/users/{id}", testUser.getId()))
                .andExpect(status().isNoContent());

        // Verify user is deleted from repository
        Optional<User> deletedUser = userRepository.findById(testUser.getId());
        assertThat(deletedUser).isEmpty();
    }

    @Test
    @DisplayName("Integration test - Delete user - Not found")
    void testDeleteUser_NotFound() throws Exception {
        // Perform DELETE request for non-existent user
        mockMvc.perform(delete("/api/users/{id}", "nonexistentid"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Integration test - Create user with validation error")
    void testCreateUser_ValidationError() throws Exception {
        // Create invalid user (missing required fields)
        User invalidUser = User.builder()
                .id("invalid-user")
                .username("") // Empty username (invalid)
                .email("invalid@example.com")
                .passwordHash("hashedpassword")
                .role(UserRole.EMPLOYEE)
                .build();

        // Perform POST request with invalid user
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());

        // Verify user is not saved in repository
        Optional<User> savedUser = userRepository.findById(invalidUser.getId());
        assertThat(savedUser).isEmpty();
    }
}
