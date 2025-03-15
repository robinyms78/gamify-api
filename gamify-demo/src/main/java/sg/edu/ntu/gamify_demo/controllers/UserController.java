package sg.edu.ntu.gamify_demo.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sg.edu.ntu.gamify_demo.exceptions.UserNotFoundException;
import sg.edu.ntu.gamify_demo.exceptions.UserValidationException;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.interfaces.UserService;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "Endpoints for user CRUD operations")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a user", description = "Registers a new user in the system")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User created successfully",
            content = @Content(schema = @Schema(implementation = User.class))),
        @ApiResponse(responseCode = "400", description = "Invalid user data",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "409", description = "Username/email exists",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(hidden = true)))
    })
    public User createUser(
        @Parameter(description = "User object to create", required = true,
                  content = @Content(schema = @Schema(implementation = User.class)))
        @RequestBody User user) throws UserValidationException {
        return userService.createUser(user);
    }


    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves a single user's details")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User found",
            content = @Content(schema = @Schema(implementation = User.class))),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public User getUser(
        @Parameter(description = "ID of the user to retrieve", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable String id) throws UserNotFoundException {
        return userService.getUserById(id);
    }

    @GetMapping
    @Operation(summary = "List all users", description = "Retrieves a list of all registered users")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list",
               content = @Content(schema = @Schema(implementation = List.class)))
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Updates an existing user's details")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User updated successfully",
            content = @Content(schema = @Schema(implementation = User.class))),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "400", description = "Invalid update data")
    })
    public User updateUser(
        @Parameter(description = "ID of the user to update", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable String id,
        @Parameter(description = "Updated user object", required = true,
                  content = @Content(schema = @Schema(implementation = User.class)))
        @RequestBody User user) 
            throws UserNotFoundException, UserValidationException {
        return userService.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete user", description = "Removes a user from the system")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public void deleteUser(
        @Parameter(description = "ID of the user to delete", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable String id) throws UserNotFoundException {
        userService.deleteUser(id);
    }
}
