package sg.edu.ntu.gamify_demo.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sg.edu.ntu.gamify_demo.services.LadderService; 
import sg.edu.ntu.gamify_demo.interfaces.UserService;
import sg.edu.ntu.gamify_demo.dtos.LadderStatusDTO;
import sg.edu.ntu.gamify_demo.mappers.LadderStatusMapper;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.exceptions.UserNotFoundException;
import sg.edu.ntu.gamify_demo.facades.GamificationFacade;
import sg.edu.ntu.gamify_demo.models.LadderLevel;
import sg.edu.ntu.gamify_demo.models.UserLadderStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import sg.edu.ntu.gamify_demo.dtos.ErrorResponse;
import sg.edu.ntu.gamify_demo.dtos.ErrorResponseDTO;

/**
 * REST controller for ladder-related endpoints.
 */
@RestController
@RequestMapping("/api/ladder")
@Tag(name = "Ladder System", description = "Manage user rankings and level progression")
@SecurityRequirement(name = "bearerAuth")
public class LadderController {
    
    @Autowired
    private LadderService ladderService;
    
    @Autowired
    private GamificationFacade gamificationFacade;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Get all ladder levels.
     * 
     * @return A map of level numbers to points required.
     */
    @GetMapping("/levels")
    @Operation(summary = "Get all levels", 
              description = "Retrieve all ladder levels with point requirements")
    @ApiResponse(responseCode = "200", description = "Success",
                content = @Content(schema = @Schema(example = """
                    {
                      "1": 100,
                      "2": 300,
                      "3": 600
                    }""")))
    public ResponseEntity<Map<Long, Long>> getLadderLevels() {
        Map<Long, Long> levels = ladderService.getLadderLevels();
        return ResponseEntity.ok(levels);
    }
    
    /**
     * Get a user's current ladder status using path variable.
     * 
     * @param userId The ID of the user.
     * @return The user's ladder status.
     */
    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user's ladder status", 
              description = "Retrieve detailed level progression information")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(schema = @Schema(implementation = LadderStatusDTO.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<?> getUserLadderStatus(
        @Parameter(description = "User ID to retrieve status for", required = true, example = "user-123")
        @PathVariable String userId) {
        try {
            System.out.println("LadderController: Getting ladder status for user: " + userId);
            UserLadderStatus status = ladderService.getUserLadderStatus(userId);
            
            if (status == null) {
                System.out.println("LadderController: User ladder status not found for: " + userId);
                
                // Try to recover by checking if the user exists
                User user = userService.getUserById(userId);
                if (user != null) {
                    System.out.println("LadderController: User exists, attempting to initialize ladder status");
                    // User exists but no ladder status, try to initialize it
                    status = ladderService.initializeUserLadderStatus(user);
                    if (status != null) {
                        System.out.println("LadderController: Successfully initialized ladder status for: " + userId);
                        LadderStatusDTO dto = LadderStatusMapper.INSTANCE.toDTO(status);
                        return ResponseEntity.ok(dto);
                    }
                }
                
                throw new UserNotFoundException(userId);
            }
            
            System.out.println("LadderController: Successfully retrieved ladder status for: " + userId);
            LadderStatusDTO dto = LadderStatusMapper.INSTANCE.toDTO(status);
            return ResponseEntity.ok(dto);
        } catch (UserNotFoundException e) {
            throw e; // Let the exception handler deal with this
        } catch (DataAccessException e) {
            System.err.println("LadderController: Database error getting ladder status: " + e.getMessage());
            e.printStackTrace();
            
            ErrorResponseDTO errorResponse = new ErrorResponseDTO();
            errorResponse.setError("Database error");
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (Exception e) {
            System.err.println("LadderController: Unexpected error getting ladder status: " + e.getMessage());
            e.printStackTrace();
            
            ErrorResponseDTO errorResponse = new ErrorResponseDTO();
            errorResponse.setError("Server error");
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get a user's ladder status with query parameter.
     * Returns current level, earned points, points to next level, and level label.
     * 
     * @param userId The ID of the user as a query parameter.
     * @return The user's ladder status as a DTO.
     */
    @GetMapping("/status")
    @Operation(summary = "Get ladder status via query", 
              description = "Retrieve ladder status using query parameter")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved status",
                    content = @Content(schema = @Schema(implementation = LadderStatusDTO.class))),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<LadderStatusDTO> getLadderStatus(
        @Parameter(description = "User ID as query parameter", required = true, example = "user-456")
        @RequestParam String userId) {
        LadderStatusDTO status = gamificationFacade.getUserLadderStatus(userId);
        
        if (status == null) {
            throw new UserNotFoundException(userId);
        }
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * Update a user's ladder status based on their earned points.
     * 
     * @param userId The ID of the user.
     * @return The updated ladder status.
     */
    @PutMapping("/users/{userId}")
    @Operation(summary = "Update ladder status", 
              description = "Recalculate and update a user's ladder position")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status updated successfully",
                    content = @Content(schema = @Schema(implementation = LadderStatusDTO.class))),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<LadderStatusDTO> updateUserLadderStatus(
        @Parameter(description = "User ID to update", required = true, example = "user-789")
        @PathVariable String userId) {
        UserLadderStatus status = ladderService.updateUserLadderStatus(userId);
        
        if (status == null) {
            throw new UserNotFoundException(userId);
        }
        
        LadderStatusDTO dto = LadderStatusMapper.INSTANCE.toDTO(status);
        return ResponseEntity.ok(dto);
    }
    
    /**
     * Get the label for a specific ladder level.
     * 
     * @param level The level number.
     * @return The label for the level.
     */
    @GetMapping("/levels/{level}/label")
    @Operation(summary = "Get level label", 
              description = "Retrieve the display label for a specific level")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved label",
                    content = @Content(schema = @Schema(implementation = ObjectNode.class))),
        @ApiResponse(responseCode = "404", description = "Level not found")
    })
    public ResponseEntity<ObjectNode> getLevelLabel(
        @Parameter(description = "Level number to query", required = true, example = "5")
        @PathVariable int level) {
        String label = ladderService.getLevelLabel(level);
        
        ObjectNode result = objectMapper.createObjectNode();
        result.put("level", level);
        result.put("label", label);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Create a new ladder level.
     * 
     * @param levelData JSON data containing level, label, and pointsRequired.
     * @return The created ladder level.
     */
    @PostMapping("/levels")
    @Operation(summary = "Create new level", 
              description = "Add a new level to the ladder system")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created",
                    content = @Content(schema = @Schema(implementation = LadderLevel.class,
                        example = """
                            {
                              "level": 5,
                              "label": "Master Explorer",
                              "pointsRequired": 1000
                            }"""))),
        @ApiResponse(responseCode = "400", description = "Invalid data",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<LadderLevel> createLadderLevel(
        @Parameter(description = "Level data in JSON format", required = true,
                  content = @Content(schema = @Schema(example = """
                      {
                          "level": 5,
                          "label": "Senior Adventurer",
                          "pointsRequired": 1000
                      }""")))
        @RequestBody JsonNode levelData) {
        int level = levelData.get("level").asInt();
        String label = levelData.get("label").asText();
        int pointsRequired = levelData.get("pointsRequired").asInt();
        
        LadderLevel ladderLevel = ladderService.createLadderLevel(level, label, pointsRequired);
        return new ResponseEntity<>(ladderLevel, HttpStatus.CREATED);
    }
    
    /**
     * Update an existing ladder level.
     * 
     * @param level The level number to update.
     * @param levelData JSON data containing label and pointsRequired.
     * @return The updated ladder level.
     */
    @PutMapping("/levels/{level}")
    @Operation(summary = "Update existing level", 
              description = "Modify an existing ladder level configuration")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Level updated successfully",
                    content = @Content(schema = @Schema(implementation = LadderLevel.class))),
        @ApiResponse(responseCode = "404", description = "Level not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid update data",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<LadderLevel> updateLadderLevel(
        @Parameter(description = "Level number to update", 
                   example = "3",
                   required = true)
        @PathVariable int level,
        @RequestBody 
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Level update data",
            content = @Content(examples = @ExampleObject("""
                {
                  "label": "Advanced Explorer",
                  "pointsRequired": 800
                }""")))
        JsonNode levelData) {
        
        String label = levelData.get("label").asText();
        int pointsRequired = levelData.get("pointsRequired").asInt();
        
        LadderLevel ladderLevel = ladderService.updateLadderLevel(level, label, pointsRequired);
        
        if (ladderLevel == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(ladderLevel);
    }
    
    /**
     * Delete a ladder level.
     * 
     * @param level The level number to delete.
     * @return No content response if successful, or an error message if the level cannot be deleted.
     */
    @DeleteMapping("/levels/{level}")
    @Operation(summary = "Delete level", 
              description = "Remove a level from the ladder system")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Level deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Level not found"),
        @ApiResponse(responseCode = "409", description = "Level cannot be deleted (active users)")
    })
    public ResponseEntity<?> deleteLadderLevel(
        @Parameter(description = "Level number to delete", required = true, example = "2")
        @PathVariable int level) {
        boolean deleted = ladderService.deleteLadderLevel(level);
        
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            ErrorResponseDTO errorResponse = new ErrorResponseDTO();
            errorResponse.setError("Cannot delete level");
            errorResponse.setMessage("The level does not exist or users are currently at this level");
            
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
    }
    
    /**
     * Exception handler for UserNotFoundException.
     * 
     * @param ex The exception.
     * @return Error response.
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFoundException(UserNotFoundException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO();
        errorResponse.setError("User not found");
        errorResponse.setMessage(ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
}
