package sg.edu.ntu.gamify_demo.controllers;
import java.util.Optional;
import java.net.URI;
import java.util.UUID;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import sg.edu.ntu.gamify_demo.dtos.AuthResponse;
import sg.edu.ntu.gamify_demo.dtos.LoginRequest;
import sg.edu.ntu.gamify_demo.dtos.RegistrationRequest;
import sg.edu.ntu.gamify_demo.dtos.RegistrationResponse;
import sg.edu.ntu.gamify_demo.exceptions.AuthenticationException;
import sg.edu.ntu.gamify_demo.exceptions.DuplicateUserException;
import sg.edu.ntu.gamify_demo.interfaces.LeaderboardService;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.repositories.UserRepository;
import sg.edu.ntu.gamify_demo.services.AuthenticationService;

/**
 * Controller for handling authentication-related endpoints.
 * Provides endpoints for user registration and login.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
@SecurityRequirement(name = "bearerAuth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationService authService;
    private final LeaderboardService leaderboardService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                          AuthenticationService authService, LeaderboardService leaderboardService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
        this.leaderboardService = leaderboardService;
    }

    /**
     * Endpoint for registering a new user.
     * 
     * @param request The registration request containing user details
     * @return A response with a success message and the new user's ID
     * @throws DuplicateUserException if the username or email already exists
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user",
        description = "Creates a new user account with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = RegistrationResponse.class))),
        @ApiResponse(responseCode = "409", description = "Conflict - Username or email already exists",
            content = @Content(examples = {
                @ExampleObject(value = "{\"error\": \"Registration failed\", \"message\": \"Username already exists\"}"),
                @ExampleObject(value = "{\"error\": \"Registration failed\", \"message\": \"Email already exists\"}")
            })),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data")
    })
    public ResponseEntity<RegistrationResponse> registerUser(
            @Parameter(description = "Registration request containing user details",
                required = true,
                content = @Content(schema = @Schema(implementation = RegistrationRequest.class)))
            @Valid @RequestBody RegistrationRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateUserException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateUserException("Email already exists");
        }

        // Create new user
        User newUser = User.builder()
                .id(UUID.randomUUID().toString())
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .department(request.department())
                .earnedPoints(0L)
                .availablePoints(0L)
                .build();

        // Save user to database
        User savedUser = userRepository.save(newUser);
        
        try {
            // Create leaderboard entry for the new user
            leaderboardService.createLeaderboardEntry(savedUser);
        } catch (Exception e) {
            // Log the error but continue with user registration
            System.err.println("Error creating leaderboard entry: " + e.getMessage());
            // The leaderboard entry will be created later when the user interacts with the system
        }

        // Build location URI with full context path
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/users/{id}")
                .buildAndExpand(savedUser.getId())
                .toUri();

        return ResponseEntity.created(location)
                .body(new RegistrationResponse("User registered successfully", savedUser.getId()));
    }

    /**
     * Endpoint for user login.
     * 
     * @param request The login request containing username and password
     * @return A response with a JWT token and user details
     * @throws AuthenticationException if the credentials are invalid
     */
    @PostMapping("/login")
    @Operation(summary = "Authenticate user",
        description = "Authenticates user credentials and returns JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials",
            content = @Content(examples = {
                @ExampleObject(value = "{\"error\": \"Authentication failed\", \"message\": \"Invalid credentials\"}")
            }))
    })
    public ResponseEntity<AuthResponse> loginUser(
            @Parameter(description = "Login request containing credentials",
                required = true,
                content = @Content(schema = @Schema(implementation = LoginRequest.class)))
            @RequestBody LoginRequest request) {
        // Find user by username
        Optional<User> optionalUser = userRepository.findByUsername(request.username());
        
        if (optionalUser.isEmpty()) {
            throw new AuthenticationException("Invalid credentials");
        }

        User user = optionalUser.get();

        // Verify password
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid credentials");
        }

        // Generate JWT token
        String token = authService.generateToken(user);

        // Return token and user details with header
        AuthResponse response = new AuthResponse(token, user);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .body(response);
    }
}
