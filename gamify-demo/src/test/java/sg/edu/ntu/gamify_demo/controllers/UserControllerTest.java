package sg.edu.ntu.gamify_demo.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import sg.edu.ntu.gamify_demo.exceptions.UserNotFoundException;
import sg.edu.ntu.gamify_demo.exceptions.UserValidationException;
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.enums.UserRole;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;
    
    @InjectMocks
    private UserController userController;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    
    // Test data
    private User testUser1;
    private User testUser2;
    private String testUserId1;
    private String testUserId2;
    
    @BeforeEach
    void setUp() {
        // Setup MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Initialize test data
        testUserId1 = UUID.randomUUID().toString();
        testUserId2 = UUID.randomUUID().toString();
        
        testUser1 = User.builder()
                .id(testUserId1)
                .username("testuser1")
                .email("test1@example.com")
                .passwordHash("hashedpassword1")
                .role(UserRole.EMPLOYEE)
                .department("Engineering")
                .earnedPoints(100L)
                .availablePoints(100L)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();
        
        testUser2 = User.builder()
                .id(testUserId2)
                .username("testuser2")
                .email("test2@example.com")
                .passwordHash("hashedpassword2")
                .role(UserRole.MANAGER)
                .department("Marketing")
                .earnedPoints(200L)
                .availablePoints(150L)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();
    }
    
    @Test
    @DisplayName("Create user should return 201 Created and the created user")
    void createUser_ShouldReturnCreatedUser() throws Exception {
        // Arrange
        when(userService.createUser(any(User.class))).thenReturn(testUser1);
        
        // Act & Assert
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testUserId1))
                .andExpect(jsonPath("$.username").value("testuser1"))
                .andExpect(jsonPath("$.email").value("test1@example.com"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"))
                .andExpect(jsonPath("$.department").value("Engineering"))
                .andExpect(jsonPath("$.earnedPoints").value(100))
                .andExpect(jsonPath("$.availablePoints").value(100));
        
        verify(userService).createUser(any(User.class));
    }
    
    @Test
    @DisplayName("Create user should handle validation exception")
    void createUser_ShouldHandleValidationException() throws Exception {
        // Arrange
        when(userService.createUser(any(User.class)))
                .thenThrow(new UserValidationException("Invalid user data"));
        
        // Act & Assert
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser1)))
                .andExpect(status().isBadRequest());
        
        verify(userService).createUser(any(User.class));
    }
    
    @Test
    @DisplayName("Get user by ID should return the user when found")
    void getUser_ShouldReturnUserWhenFound() throws Exception {
        // Arrange
        when(userService.getUserById(testUserId1)).thenReturn(testUser1);
        
        // Act & Assert
        mockMvc.perform(get("/api/users/{id}", testUserId1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserId1))
                .andExpect(jsonPath("$.username").value("testuser1"))
                .andExpect(jsonPath("$.email").value("test1@example.com"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"))
                .andExpect(jsonPath("$.department").value("Engineering"))
                .andExpect(jsonPath("$.earnedPoints").value(100))
                .andExpect(jsonPath("$.availablePoints").value(100));
        
        verify(userService).getUserById(testUserId1);
    }
    
    @Test
    @DisplayName("Get user by ID should return 404 when user not found")
    void getUser_ShouldReturn404WhenUserNotFound() throws Exception {
        // Arrange
        when(userService.getUserById("nonexistent"))
                .thenThrow(new UserNotFoundException("User not found with id: nonexistent"));
        
        // Act & Assert
        mockMvc.perform(get("/api/users/{id}", "nonexistent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        
        verify(userService).getUserById("nonexistent");
    }
    
    @Test
    @DisplayName("Get all users should return a list of users")
    void getAllUsers_ShouldReturnListOfUsers() throws Exception {
        // Arrange
        List<User> users = Arrays.asList(testUser1, testUser2);
        when(userService.getAllUsers()).thenReturn(users);
        
        // Act & Assert
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testUserId1))
                .andExpect(jsonPath("$[0].username").value("testuser1"))
                .andExpect(jsonPath("$[1].id").value(testUserId2))
                .andExpect(jsonPath("$[1].username").value("testuser2"));
        
        verify(userService).getAllUsers();
    }
    
    @Test
    @DisplayName("Update user should return the updated user")
    void updateUser_ShouldReturnUpdatedUser() throws Exception {
        // Arrange
        User updatedUser = User.builder()
                .id(testUserId1)
                .username("testuser1updated")
                .email("test1updated@example.com")
                .passwordHash("hashedpassword1")
                .role(UserRole.EMPLOYEE)
                .department("QA")
                .earnedPoints(150L)
                .availablePoints(150L)
                .build();
        
        when(userService.updateUser(eq(testUserId1), any(User.class))).thenReturn(updatedUser);
        
        // Act & Assert
        mockMvc.perform(put("/api/users/{id}", testUserId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserId1))
                .andExpect(jsonPath("$.username").value("testuser1updated"))
                .andExpect(jsonPath("$.email").value("test1updated@example.com"))
                .andExpect(jsonPath("$.department").value("QA"))
                .andExpect(jsonPath("$.earnedPoints").value(150))
                .andExpect(jsonPath("$.availablePoints").value(150));
        
        verify(userService).updateUser(eq(testUserId1), any(User.class));
    }
    
    @Test
    @DisplayName("Update user should return 404 when user not found")
    void updateUser_ShouldReturn404WhenUserNotFound() throws Exception {
        // Arrange
        when(userService.updateUser(eq("nonexistent"), any(User.class)))
                .thenThrow(new UserNotFoundException("User not found with id: nonexistent"));
        
        // Act & Assert
        mockMvc.perform(put("/api/users/{id}", "nonexistent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser1)))
                .andExpect(status().isNotFound());
        
        verify(userService).updateUser(eq("nonexistent"), any(User.class));
    }
    
    @Test
    @DisplayName("Update user should handle validation exception")
    void updateUser_ShouldHandleValidationException() throws Exception {
        // Arrange
        when(userService.updateUser(eq(testUserId1), any(User.class)))
                .thenThrow(new UserValidationException("Invalid user data"));
        
        // Act & Assert
        mockMvc.perform(put("/api/users/{id}", testUserId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser1)))
                .andExpect(status().isBadRequest());
        
        verify(userService).updateUser(eq(testUserId1), any(User.class));
    }
    
    @Test
    @DisplayName("Delete user should return 204 No Content")
    void deleteUser_ShouldReturn204NoContent() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUser(testUserId1);
        
        // Act & Assert
        mockMvc.perform(delete("/api/users/{id}", testUserId1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        
        verify(userService).deleteUser(testUserId1);
    }
    
    @Test
    @DisplayName("Delete user should return 404 when user not found")
    void deleteUser_ShouldReturn404WhenUserNotFound() throws Exception {
        // Arrange
        doThrow(new UserNotFoundException("User not found with id: nonexistent"))
                .when(userService).deleteUser("nonexistent");
        
        // Act & Assert
        mockMvc.perform(delete("/api/users/{id}", "nonexistent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        
        verify(userService).deleteUser("nonexistent");
    }
}
