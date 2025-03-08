package sg.edu.ntu.gamify_demo.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import sg.edu.ntu.gamify_demo.dtos.LoginRequest;
import sg.edu.ntu.gamify_demo.dtos.RegistrationRequest;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.enums.UserRole;
import sg.edu.ntu.gamify_demo.repositories.UserRepository;
import sg.edu.ntu.gamify_demo.services.AuthenticationService;

/**
 * Test class for AuthController.
 * Tests the registration and login endpoints.
 */
@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AuthenticationService authService;

    private User testUser;
    private RegistrationRequest registrationRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = User.builder()
                .id("test-user-id")
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .role(UserRole.EMPLOYEE)
                .department("Testing")
                .earnedPoints(0)
                .availablePoints(0)
                .build();

        // Create registration request
        registrationRequest = new RegistrationRequest(
                "testuser",
                "test@example.com",
                "password123",
                UserRole.EMPLOYEE,
                "Testing"
        );

        // Create login request
        loginRequest = new LoginRequest(
                "testuser",
                "password123"
        );
    }

    @Test
    @DisplayName("Test user registration - Success scenario")
    void testRegisterUser_Success() throws Exception {
        // Mock repository methods
        when(userRepository.existsByUsername(registrationRequest.username())).thenReturn(false);
        when(userRepository.existsByEmail(registrationRequest.email())).thenReturn(false);
        when(passwordEncoder.encode(registrationRequest.password())).thenReturn("hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Perform POST request
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.userId").value(testUser.getId()));
    }

    @Test
    @DisplayName("Test user registration - Username already exists")
    void testRegisterUser_UsernameExists() throws Exception {
        // Mock repository methods
        when(userRepository.existsByUsername(registrationRequest.username())).thenReturn(true);

        // Perform POST request
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Registration failed"))
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    @DisplayName("Test user registration - Email already exists")
    void testRegisterUser_EmailExists() throws Exception {
        // Mock repository methods
        when(userRepository.existsByUsername(registrationRequest.username())).thenReturn(false);
        when(userRepository.existsByEmail(registrationRequest.email())).thenReturn(true);

        // Perform POST request
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Registration failed"))
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    @DisplayName("Test user login - Success scenario")
    void testLoginUser_Success() throws Exception {
        // Mock repository and service methods
        when(userRepository.findByUsername(loginRequest.username())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.password(), testUser.getPasswordHash())).thenReturn(true);
        when(authService.generateToken(testUser)).thenReturn("jwt-token");

        // Perform POST request
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.user.id").value(testUser.getId()))
                .andExpect(jsonPath("$.user.username").value(testUser.getUsername()))
                .andExpect(jsonPath("$.user.email").value(testUser.getEmail()));
    }

    @Test
    @DisplayName("Test user login - User not found")
    void testLoginUser_UserNotFound() throws Exception {
        // Mock repository methods
        when(userRepository.findByUsername(loginRequest.username())).thenReturn(Optional.empty());

        // Perform POST request
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication failed"))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    @DisplayName("Test user login - Invalid password")
    void testLoginUser_InvalidPassword() throws Exception {
        // Mock repository and service methods
        when(userRepository.findByUsername(loginRequest.username())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.password(), testUser.getPasswordHash())).thenReturn(false);

        // Perform POST request
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication failed"))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }
}
