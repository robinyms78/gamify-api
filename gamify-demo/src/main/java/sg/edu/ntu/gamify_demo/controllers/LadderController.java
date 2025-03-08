package sg.edu.ntu.gamify_demo.controllers;

import java.util.List;
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

/**
 * REST controller for ladder-related endpoints.
 */
@RestController
@RequestMapping("/api/ladder")
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
    public ResponseEntity<UserLadderStatus> getUserLadderStatus(@PathVariable String userId) {
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
    public ResponseEntity<LadderStatusDTO> getLadderStatus(@RequestParam String userId) {
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
    public ResponseEntity<UserLadderStatus> updateUserLadderStatus(@PathVariable String userId) {
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
    public ResponseEntity<ObjectNode> getLevelLabel(@PathVariable int level) {
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
    public ResponseEntity<LadderLevel> createLadderLevel(@RequestBody JsonNode levelData) {
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
    public ResponseEntity<LadderLevel> updateLadderLevel(
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
    public ResponseEntity<ObjectNode> deleteLadderLevel(@PathVariable int level) {
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
