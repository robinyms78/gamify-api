package sg.edu.ntu.gamify_demo.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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

import sg.edu.ntu.gamify_demo.dtos.LadderStatusDTO;
import sg.edu.ntu.gamify_demo.exceptions.UserNotFoundException;
import sg.edu.ntu.gamify_demo.facades.GamificationFacade;
import sg.edu.ntu.gamify_demo.models.LadderLevel;
import sg.edu.ntu.gamify_demo.models.UserLadderStatus;
import sg.edu.ntu.gamify_demo.services.LadderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST controller for ladder-related endpoints.
 */
@RestController
@RequestMapping("/api/ladder")
@Tag(name = "Ladder System", description = "Manage user rankings and level progression")
public class LadderController {
    
    @Autowired
    private LadderService ladderService;
    
    @Autowired
    private GamificationFacade gamificationFacade;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Get all ladder levels.
     * 
     * @return A map of level numbers to points required.
     */
    @GetMapping("/levels")
    @Operation(summary = "Get all levels", 
              description = "Retrieve all ladder levels with their point requirements")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved levels listing")
    public ResponseEntity<Map<Integer, Integer>> getLadderLevels() {
        Map<Integer, Integer> levels = ladderService.getLadderLevels();
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
              description = "Retrieve a user's current ladder position and progress")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved user status",
                    content = @Content(schema = @Schema(implementation = UserLadderStatus.class))),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserLadderStatus> getUserLadderStatus(
        @Parameter(description = "User ID to retrieve status for", required = true, example = "user-123")
        @PathVariable String userId) {
        UserLadderStatus status = ladderService.getUserLadderStatus(userId);
        
        if (status == null) {
            throw new UserNotFoundException(userId);
        }
        
        return ResponseEntity.ok(status);
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
                    content = @Content(schema = @Schema(implementation = UserLadderStatus.class))),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserLadderStatus> updateUserLadderStatus(
        @Parameter(description = "User ID to update", required = true, example = "user-789")
        @PathVariable String userId) {
        UserLadderStatus status = ladderService.updateUserLadderStatus(userId);
        
        if (status == null) {
            throw new UserNotFoundException(userId);
        }
        
        return ResponseEntity.ok(status);
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
        @ApiResponse(responseCode = "201", description = "Level created successfully",
                    content = @Content(schema = @Schema(implementation = LadderLevel.class))),
        @ApiResponse(responseCode = "400", description = "Invalid level data")
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
        @ApiResponse(responseCode = "404", description = "Level not found"),
        @ApiResponse(responseCode = "400", description = "Invalid update data")
    })
    public ResponseEntity<LadderLevel> updateLadderLevel(
        @Parameter(description = "Level number to update", required = true, example = "3")
        @PathVariable int level,
        @RequestBody JsonNode levelData) {
        
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
    public ResponseEntity<ObjectNode> deleteLadderLevel(
        @Parameter(description = "Level number to delete", required = true, example = "2")
        @PathVariable int level) {
        boolean deleted = ladderService.deleteLadderLevel(level);
        
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            ObjectNode errorJson = objectMapper.createObjectNode();
            errorJson.put("error", "Cannot delete level");
            errorJson.put("message", "The level does not exist or users are currently at this level");
            
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorJson);
        }
    }
    
    /**
     * Exception handler for UserNotFoundException.
     * 
     * @param ex The exception.
     * @return Error response.
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ObjectNode> handleUserNotFoundException(UserNotFoundException ex) {
        ObjectNode errorJson = objectMapper.createObjectNode();
        errorJson.put("error", "User not found");
        errorJson.put("message", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorJson);
    }
}
