package sg.edu.ntu.gamify_demo.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import sg.edu.ntu.gamify_demo.config.SecurityConfig;
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
@Import({
    TestIntegrationConfig.class,
    SecurityConfig.class // Add this line
})
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
        userRepository.deleteAll();
        
        // Generate unique identifiers for each test run
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String testUsername = "testuser-" + uniqueId;
        String testEmail = "test-" + uniqueId + "@example.com";

        registrationRequest = new RegistrationRequest(
            testUsername,
            testEmail,
            "ValidPass123!",
            UserRole.EMPLOYEE,
            "Test Department"
        );

        loginRequest = new LoginRequest(testUsername, "ValidPass123!");
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Integration test - Complete registration and authentication flow")
    void testRegisterAndLoginFlow() throws Exception {
        // Registration
        MvcResult registrationResult = mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registrationRequest)))
            .andExpectAll(
                status().isCreated(),
                jsonPath("$.userId").isNotEmpty(),
                jsonPath("$.password").doesNotExist(), // Ensure password not leaked
                header().exists(HttpHeaders.LOCATION)
            )
            .andReturn();

        // Extract user ID from registration response
        String responseJson = registrationResult.getResponse().getContentAsString();
        String userId = objectMapper.readTree(responseJson).get("userId").asText();

        // Verify user is saved in repository with correct initial points
        assertThat(userRepository.findById(userId)).isPresent();
        assertThat(userRepository.findById(userId).get().getEarnedPoints()).isEqualTo(0);
        assertThat(userRepository.findById(userId).get().getAvailablePoints()).isEqualTo(0);

        // Login
        mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.token").isNotEmpty(),
                jsonPath("$.user.id").exists(),
                jsonPath("$.user.password").doesNotExist(),
                header().exists(HttpHeaders.AUTHORIZATION)
            );
    }

    @ParameterizedTest
    @MethodSource("invalidRegistrationPayloads")
    @DisplayName("Integration test - Register with invalid payload")
    void testRegisterWithInvalidPayload(RegistrationRequest invalidRequest, String expectedError) throws Exception {
        mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpectAll(
                status().isBadRequest(),
                jsonPath("$.error").value("Validation error"),
                jsonPath("$.details[0]").value(containsString(expectedError))
            );
    }

    private static Stream<Arguments> invalidRegistrationPayloads() {
        return Stream.of(
            // Test case 1: Missing username but valid password
            Arguments.of(
                new RegistrationRequest(
                    null, 
                    "valid@email.com", 
                    "ValidPass123!", // Valid password
                    UserRole.EMPLOYEE, 
                    "Dept"
                ),
                "username: Username is required"
            ),
            // Test case 2: Invalid email with valid password
            Arguments.of(
                new RegistrationRequest(
                    "user", 
                    "invalid-email", 
                    "ValidPass123!", // Valid password
                    UserRole.EMPLOYEE, 
                    "Dept"
                ),
                "email: Must be a valid email address"
            ),
            // Test case 3: Short password with valid email
            Arguments.of(
                new RegistrationRequest(
                    "user", 
                    "valid@email.com", 
                    "short", 
                    UserRole.EMPLOYEE, 
                    "Dept"
                ),
                "password: Password must be at least 8 characters"
            )
        );
    }

    @Test
    @DisplayName("Integration test - Security headers validation")
    void testSecurityHeaders() throws Exception {
        mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registrationRequest)))
            .andExpectAll(
                header().string("Content-Security-Policy", "default-src 'self'"),
                header().string("X-Content-Type-Options", "nosniff"),
                header().string("X-Frame-Options", "DENY")
            );
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
