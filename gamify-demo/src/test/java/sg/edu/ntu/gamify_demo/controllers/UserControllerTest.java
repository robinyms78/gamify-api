package sg.edu.ntu.gamify_demo.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fasterxml.jackson.databind.ObjectMapper;

import sg.edu.ntu.gamify_demo.config.SecurityConfig;

import sg.edu.ntu.gamify_demo.exceptions.UserNotFoundException;
import sg.edu.ntu.gamify_demo.exceptions.UserValidationException;
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.models.enums.UserRole;

/**
 * Test class for UserController.
 * Uses @WebMvcTest which focuses only on the web layer.
 */
@WebMvcTest(UserController.class)
@WithMockUser(username = "testuser", roles = {"EMPLOYEE"})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean(name = "securityConfig")
    private SecurityConfig securityConfig;

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
    void testCreateUser_Success() throws Exception {
        // Create request without server-generated fields
        User createRequest = User.builder()
                .username("testuser1")
                .email("test1@example.com")
                .passwordHash("hashedpassword1")
                .role(UserRole.EMPLOYEE)
                .department("Engineering")
                .build();

        when(userService.createUser(any(User.class))).thenReturn(testUser1);

        mockMvc.perform(post("/api/users")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))) // Use cleaned request
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)) // Tolerate charset
                .andExpect(jsonPath("$.id", is(testUser1.getId())))
                .andExpect(jsonPath("$.username", is(testUser1.getUsername())))
                .andExpect(jsonPath("$.email", is(testUser1.getEmail())))
                .andExpect(jsonPath("$.department", is(testUser1.getDepartment())))
                .andExpect(jsonPath("$.earnedPoints", is(testUser1.getEarnedPoints().intValue())))
                .andExpect(jsonPath("$.availablePoints", is(testUser1.getAvailablePoints().intValue())));
    }

    @Test
    @DisplayName("Test create user - Validation failure")
    void testCreateUser_ValidationFailure() throws Exception {
        // Mock service to throw validation exception
        UserValidationException exception = new UserValidationException("Validation failed");
        when(userService.createUser(any(User.class))).thenThrow(exception);

        // Perform POST request
        mockMvc.perform(post("/api/users")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser1)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test get user by ID - Success scenario")
    void testGetUserById_Success() throws Exception {
        // Mock service to return the user
        when(userService.getUserById(testUser1.getId())).thenReturn(testUser1);

        // Perform GET request
        mockMvc.perform(get("/api/users/{id}", testUser1.getId())
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testUser1.getId())))
                .andExpect(jsonPath("$.username", is(testUser1.getUsername())))
                .andExpect(jsonPath("$.email", is(testUser1.getEmail())))
                .andExpect(jsonPath("$.role", is(testUser1.getRole().toString())));
    }

    @Test
    @DisplayName("Test get user by ID - User not found")
    void testGetUserById_UserNotFound() throws Exception {
        // Mock service to throw not found exception
        UserNotFoundException exception = new UserNotFoundException("User not found");
        when(userService.getUserById(anyString())).thenThrow(exception);

        // Perform GET request
        mockMvc.perform(get("/api/users/{id}", "nonexistentid")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test get all users")
    void testGetAllUsers() throws Exception {
        List<User> users = Arrays.asList(testUser1, testUser2);
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].department", is("Engineering"))) // New checks
                .andExpect(jsonPath("$[1].role", is("MANAGER")));
    }

    @Test
    @DisplayName("Test update user - Success scenario")
    void testUpdateUser_Success() throws Exception {
        // Request body without ID
        User updateRequest = User.builder()
                .username(testUser1.getUsername())
                .email(testUser1.getEmail())
                .passwordHash(testUser1.getPasswordHash())
                .role(testUser1.getRole())
                .department("Research") 
                .earnedPoints(150L) 
                .availablePoints(120L) 
                .build();

        User updatedUser = User.builder()
                .id(testUser1.getId())
                .username(testUser1.getUsername())
                .email(testUser1.getEmail())
                .passwordHash(testUser1.getPasswordHash())
                .role(testUser1.getRole())
                .department("Research")
                .earnedPoints(150L)
                .availablePoints(120L)
                .build();

        when(userService.updateUser(eq(testUser1.getId()), any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/{id}", testUser1.getId())
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))) // No ID in body
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.department", is("Research")))
                .andExpect(jsonPath("$.earnedPoints", is(150)))
                .andExpect(jsonPath("$.availablePoints", is(120)));
    }

    @Test
    @DisplayName("Test update user - User not found")
    void testUpdateUser_UserNotFound() throws Exception {
        // Mock service to throw not found exception
        UserNotFoundException exception = new UserNotFoundException("User not found");
        when(userService.updateUser(anyString(), any(User.class))).thenThrow(exception);

        // Perform PUT request
        mockMvc.perform(put("/api/users/{id}", "nonexistentid")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser1)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test update user - Validation failure")
    void testUpdateUser_ValidationFailure() throws Exception {
        // Mock service to throw validation exception
        UserValidationException exception = new UserValidationException("Validation failed");
        when(userService.updateUser(anyString(), any(User.class))).thenThrow(exception);

        // Perform PUT request
        mockMvc.perform(put("/api/users/{id}", testUser1.getId())
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser1)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test delete user - Success scenario")
    void testDeleteUser_Success() throws Exception {
        // Mock service to do nothing (delete successful)
        doNothing().when(userService).deleteUser(testUser1.getId());

        // Perform DELETE request
        mockMvc.perform(delete("/api/users/{id}", testUser1.getId())
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Test delete user - User not found")
    void testDeleteUser_UserNotFound() throws Exception {
        // Mock service to throw not found exception
        UserNotFoundException exception = new UserNotFoundException("User not found");
        doThrow(exception).when(userService).deleteUser(anyString());

        // Perform DELETE request
        mockMvc.perform(delete("/api/users/{id}", "nonexistentid")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNotFound());
    }
}
