package sg.edu.ntu.gamify_demo.controllers;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sg.edu.ntu.gamify_demo.dtos.AuthResponse;
import sg.edu.ntu.gamify_demo.dtos.LoginRequest;
import sg.edu.ntu.gamify_demo.dtos.RegistrationRequest;
import sg.edu.ntu.gamify_demo.dtos.RegistrationResponse;
import sg.edu.ntu.gamify_demo.exceptions.AuthenticationException;
import sg.edu.ntu.gamify_demo.exceptions.DuplicateUserException;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.repositories.UserRepository;
import sg.edu.ntu.gamify_demo.services.AuthenticationService;

/**
 * Controller for handling authentication-related endpoints.
 * Provides endpoints for user registration and login.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationService authService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                          AuthenticationService authService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }

    /**
     * Endpoint for registering a new user.
     * 
     * @param request The registration request containing user details
     * @return A response with a success message and the new user's ID
     * @throws DuplicateUserException if the username or email already exists
     */
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> registerUser(@RequestBody RegistrationRequest request) {
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
                .earnedPoints(0)
                .availablePoints(0)
                .build();

        // Save user to database
        User savedUser = userRepository.save(newUser);

        // Return success response
        RegistrationResponse response = new RegistrationResponse(
                "User registered successfully", 
                savedUser.getId());
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Endpoint for user login.
     * 
     * @param request The login request containing username and password
     * @return A response with a JWT token and user details
     * @throws AuthenticationException if the credentials are invalid
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@RequestBody LoginRequest request) {
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

        // Return token and user details
        AuthResponse response = new AuthResponse(token, user);
        
        return ResponseEntity.ok(response);
    }
}
