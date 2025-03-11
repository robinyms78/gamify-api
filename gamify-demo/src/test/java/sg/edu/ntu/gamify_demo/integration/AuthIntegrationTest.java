package sg.edu.ntu.gamify_demo.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import sg.edu.ntu.gamify_demo.config.TestIntegrationConfig;
import sg.edu.ntu.gamify_demo.dtos.LoginRequest;
import sg.edu.ntu.gamify_demo.dtos.RegistrationRequest;
import sg.edu.ntu.gamify_demo.models.enums.UserRole;
import sg.edu.ntu.gamify_demo.repositories.UserRepository;

/**
 * Integration test for the authentication flow.
 * Tests the registration and login endpoints together.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Use test profile for database testing
@Import(TestIntegrationConfig.class)
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private RegistrationRequest registrationRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Clear the repository before each test
        userRepository.deleteAll();

        // Create registration request
        registrationRequest = new RegistrationRequest(
                "integrationtestuser",
                "integration@example.com",
                "password123",
                UserRole.EMPLOYEE,
                "Integration Testing"
        );

        // Create login request
        loginRequest = new LoginRequest(
                "integrationtestuser",
                "password123"
        );
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Integration test - Register and login flow")
    void testRegisterAndLoginFlow() throws Exception {
        // Step 1: Register a new user
        MvcResult registrationResult = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.userId").isNotEmpty())
                .andReturn();

        // Extract user ID from registration response
        String responseJson = registrationResult.getResponse().getContentAsString();
        String userId = objectMapper.readTree(responseJson).get("userId").asText();

        // Verify user is saved in repository with correct initial points
        assertThat(userRepository.findById(userId)).isPresent();
        assertThat(userRepository.findById(userId).get().getEarnedPoints()).isEqualTo(0);
        assertThat(userRepository.findById(userId).get().getAvailablePoints()).isEqualTo(0);

        // Step 2: Login with the registered user
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.id").value(userId))
                .andExpect(jsonPath("$.user.username").value(registrationRequest.username()))
                .andExpect(jsonPath("$.user.email").value(registrationRequest.email()))
                .andExpect(jsonPath("$.user.earnedPoints").value(0))
                .andExpect(jsonPath("$.user.availablePoints").value(0));
    }

    @Test
    @DisplayName("Integration test - Register with duplicate username")
    void testRegisterWithDuplicateUsername() throws Exception {
        // Step 1: Register a user
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated());

        // Step 2: Register another user with the same username but different email
        RegistrationRequest duplicateUsernameRequest = new RegistrationRequest(
                registrationRequest.username(),
                "different@example.com",
                "password456",
                UserRole.EMPLOYEE,
                "Different Department"
        );

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateUsernameRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Registration failed"))
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    @DisplayName("Integration test - Register with duplicate email")
    void testRegisterWithDuplicateEmail() throws Exception {
        // Step 1: Register a user
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated());

        // Step 2: Register another user with the same email but different username
        RegistrationRequest duplicateEmailRequest = new RegistrationRequest(
                "differentusername",
                registrationRequest.email(),
                "password456",
                UserRole.EMPLOYEE,
                "Different Department"
        );

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateEmailRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Registration failed"))
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    @DisplayName("Integration test - Login with invalid credentials")
    void testLoginWithInvalidCredentials() throws Exception {
        // Step 1: Register a user
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated());

        // Step 2: Login with incorrect password
        LoginRequest invalidLoginRequest = new LoginRequest(
                registrationRequest.username(),
                "wrongpassword"
        );

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication failed"))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }
}
