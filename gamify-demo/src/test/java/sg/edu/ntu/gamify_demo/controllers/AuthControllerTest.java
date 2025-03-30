package sg.edu.ntu.gamify_demo.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import sg.edu.ntu.gamify_demo.dtos.AuthResponse;
import sg.edu.ntu.gamify_demo.dtos.LoginRequest;
import sg.edu.ntu.gamify_demo.dtos.RegistrationRequest;
import sg.edu.ntu.gamify_demo.dtos.RegistrationResponse;
import sg.edu.ntu.gamify_demo.exceptions.AuthenticationException;
import sg.edu.ntu.gamify_demo.exceptions.DuplicateUserException;
import sg.edu.ntu.gamify_demo.interfaces.LeaderboardService;
import sg.edu.ntu.gamify_demo.models.LadderLevel;
import sg.edu.ntu.gamify_demo.models.Leaderboard;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.enums.UserRole;
import sg.edu.ntu.gamify_demo.repositories.UserRepository;
import sg.edu.ntu.gamify_demo.services.AuthenticationService;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private AuthenticationService authService;
    
    @Mock
    private LeaderboardService leaderboardService;
    
    @InjectMocks
    private AuthController authController;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    
    // Test data
    private User testUser;
    private String testUserId;
    private RegistrationRequest validRegistrationRequest;
    private LoginRequest validLoginRequest;
    private String testToken;
    private Leaderboard testLeaderboard;
    private LadderLevel testLadderLevel;
    
    @BeforeEach
    void setUp() {
        // Setup MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        
        objectMapper = new ObjectMapper();
        
        // Initialize test data
        testUserId = UUID.randomUUID().toString();
        testToken = "test.jwt.token";
        
        testUser = User.builder()
                .id(testUserId)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .role(UserRole.EMPLOYEE)
                .department("Engineering")
                .earnedPoints(0L)
                .availablePoints(0L)
                .build();
                
        testLadderLevel = new LadderLevel(1L, "Beginner", 0L);
        
        testLeaderboard = Leaderboard.builder()
                .userId(testUserId)
                .username("testuser")
                .department("Engineering")
                .earnedPoints(0L)
                .currentLevel(testLadderLevel)
                .rank(1L)
                .user(testUser)
                .build();
        
        validRegistrationRequest = new RegistrationRequest(
                "testuser",
                "test@example.com",
                "password123",
                UserRole.EMPLOYEE,
                "Engineering"
        );
        
        validLoginRequest = new LoginRequest(
                "testuser",
                "password123"
        );
    }
    
    @Test
    @DisplayName("Register user should create new user and return 201 Created")
    void registerUser_ShouldCreateNewUserAndReturn201Created() throws Exception {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(leaderboardService.createLeaderboardEntry(any(User.class))).thenReturn(testLeaderboard);
        
        // Act & Assert
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.userId").value(testUserId));
    }
    
    @Test
    @DisplayName("Register user should handle username already exists")
    void registerUser_ShouldHandleUsernameAlreadyExists() throws Exception {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        
        // Act & Assert
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Registration failed"))
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }
    
    @Test
    @DisplayName("Register user should handle email already exists")
    void registerUser_ShouldHandleEmailAlreadyExists() throws Exception {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        
        // Act & Assert
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Registration failed"))
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }
    
    @Test
    @DisplayName("Register user should handle invalid input data")
    void registerUser_ShouldHandleInvalidInputData() throws Exception {
        // Arrange
        RegistrationRequest invalidRequest = new RegistrationRequest(
                "", // Invalid username (empty)
                "invalid-email", // Invalid email format
                "pwd", // Invalid password (too short)
                null, // Invalid role (null)
                "" // Invalid department (empty)
        );
        
        // Act & Assert
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Login user should return JWT token and user details")
    void loginUser_ShouldReturnJwtTokenAndUserDetails() throws Exception {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "hashedpassword")).thenReturn(true);
        when(authService.generateToken(testUser)).thenReturn(testToken);
        
        // Act & Assert
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", "Bearer " + testToken))
                .andExpect(jsonPath("$.token").value(testToken))
                .andExpect(jsonPath("$.user.id").value(testUserId))
                .andExpect(jsonPath("$.user.username").value("testuser"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }
    
    @Test
    @DisplayName("Login user should handle user not found")
    void loginUser_ShouldHandleUserNotFound() throws Exception {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        
        // Act & Assert
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication failed"))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }
    
    @Test
    @DisplayName("Login user should handle invalid password")
    void loginUser_ShouldHandleInvalidPassword() throws Exception {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "hashedpassword")).thenReturn(false);
        
        // Act & Assert
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication failed"))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }
    
    @Test
    @DisplayName("Register user should handle leaderboard service exception")
    void registerUser_ShouldHandleLeaderboardServiceException() throws Exception {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(leaderboardService.createLeaderboardEntry(any(User.class))).thenThrow(new RuntimeException("Error creating leaderboard entry"));
        
        // Act & Assert
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.userId").value(testUserId));
        
        // Verify that the exception was caught and logged
        verify(leaderboardService).createLeaderboardEntry(any(User.class));
    }
}
