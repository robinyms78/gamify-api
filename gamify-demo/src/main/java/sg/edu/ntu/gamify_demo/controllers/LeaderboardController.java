package sg.edu.ntu.gamify_demo.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import sg.edu.ntu.gamify_demo.dtos.ErrorResponseDTO;
import sg.edu.ntu.gamify_demo.dtos.LeaderboardEntryDTO;
import sg.edu.ntu.gamify_demo.exceptions.UserNotFoundException;
import sg.edu.ntu.gamify_demo.interfaces.LeaderboardService;
import sg.edu.ntu.gamify_demo.mappers.LeaderboardMapper;
import sg.edu.ntu.gamify_demo.models.Leaderboard;

/**
 * REST controller for leaderboard-related endpoints.
 * Provides endpoints for retrieving global and department-specific rankings.
 */
@RestController
@RequestMapping("/api/leaderboard")
@Tag(name = "Leaderboard", description = "Endpoints for retrieving user rankings")
@SecurityRequirement(name = "bearerAuth")
public class LeaderboardController {
    
    private final LeaderboardService leaderboardService;
    private final LeaderboardMapper leaderboardMapper;
    
    @Autowired
    public LeaderboardController(LeaderboardService leaderboardService, LeaderboardMapper leaderboardMapper) {
        this.leaderboardService = leaderboardService;
        this.leaderboardMapper = leaderboardMapper;
    }
    
    /**
     * Get global rankings for all users.
     * 
     * @param page The page number (0-based).
     * @param size The page size.
     * @return A page of leaderboard entries ordered by rank.
     */
    @GetMapping("/global")
    @Operation(summary = "Get global rankings", 
              description = "Retrieve global rankings for all users")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<Page<LeaderboardEntryDTO>> getGlobalRankings(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        
        // Create pageable object
        Pageable pageable = PageRequest.of(page, size);
        
        // Get leaderboard page from service
        Page<Leaderboard> leaderboardPage = leaderboardService.getGlobalRankings(pageable);
        
        // Create empty page if service returns null
        if (leaderboardPage == null) {
            return ResponseEntity.ok(Page.empty(pageable));
        }
        
        // Convert to DTO page
        Page<LeaderboardEntryDTO> dtoPage = leaderboardPage.map(leaderboard -> {
            LeaderboardEntryDTO dto = new LeaderboardEntryDTO();
            
            // Set basic properties
            dto.setUserId(leaderboard.getUserId());
            dto.setUsername(leaderboard.getUsername());
            dto.setDepartment(leaderboard.getDepartment());
            
            // Set points with null check
            if (leaderboard.getEarnedPoints() != null) {
                dto.setEarnedPoints(leaderboard.getEarnedPoints().intValue());
            } else {
                dto.setEarnedPoints(0);
            }
            
            // Set rank with null check
            if (leaderboard.getRank() != null) {
                dto.setRank(leaderboard.getRank().intValue());
            } else {
                dto.setRank(0);
            }
            
            // Set level info with null checks
            if (leaderboard.getCurrentLevel() != null) {
                if (leaderboard.getCurrentLevel().getLevel() != null) {
                    dto.setCurrentLevel(leaderboard.getCurrentLevel().getLevel().intValue());
                } else {
                    dto.setCurrentLevel(1);
                }
                
                if (leaderboard.getCurrentLevel().getLabel() != null) {
                    dto.setLevelLabel(leaderboard.getCurrentLevel().getLabel());
                } else {
                    dto.setLevelLabel("Beginner");
                }
            } else {
                dto.setCurrentLevel(1);
                dto.setLevelLabel("Beginner");
            }
            
            return dto;
        });
        
        return ResponseEntity.ok(dtoPage);
    }
    
    /**
     * Get rankings for users in a specific department.
     * 
     * @param department The department to filter by.
     * @param page The page number (0-based).
     * @param size The page size.
     * @return A page of leaderboard entries for the specified department ordered by rank.
     */
    @GetMapping("/departments/{department}")
    @Operation(summary = "Get department rankings", 
              description = "Retrieve rankings for users in a specific department")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<Page<LeaderboardEntryDTO>> getDepartmentRankings(
            @Parameter(description = "Department name", example = "Engineering", required = true)
            @PathVariable String department,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        
        // Create pageable object
        Pageable pageable = PageRequest.of(page, size);
        
        // Handle null or empty department
        if (department == null || department.trim().isEmpty()) {
            return ResponseEntity.ok(Page.empty(pageable));
        }
        
        // Get leaderboard page from service
        Page<Leaderboard> leaderboardPage = leaderboardService.getDepartmentRankings(department, pageable);
        
        // Create empty page if service returns null
        if (leaderboardPage == null) {
            return ResponseEntity.ok(Page.empty(pageable));
        }
        
        // Convert to DTO page
        Page<LeaderboardEntryDTO> dtoPage = leaderboardPage.map(leaderboard -> {
            LeaderboardEntryDTO dto = new LeaderboardEntryDTO();
            
            // Set basic properties
            dto.setUserId(leaderboard.getUserId());
            dto.setUsername(leaderboard.getUsername());
            dto.setDepartment(leaderboard.getDepartment());
            
            // Set points with null check
            if (leaderboard.getEarnedPoints() != null) {
                dto.setEarnedPoints(leaderboard.getEarnedPoints().intValue());
            } else {
                dto.setEarnedPoints(0);
            }
            
            // Set rank with null check
            if (leaderboard.getRank() != null) {
                dto.setRank(leaderboard.getRank().intValue());
            } else {
                dto.setRank(0);
            }
            
            // Set level info with null checks
            if (leaderboard.getCurrentLevel() != null) {
                if (leaderboard.getCurrentLevel().getLevel() != null) {
                    dto.setCurrentLevel(leaderboard.getCurrentLevel().getLevel().intValue());
                } else {
                    dto.setCurrentLevel(1);
                }
                
                if (leaderboard.getCurrentLevel().getLabel() != null) {
                    dto.setLevelLabel(leaderboard.getCurrentLevel().getLabel());
                } else {
                    dto.setLevelLabel("Beginner");
                }
            } else {
                dto.setCurrentLevel(1);
                dto.setLevelLabel("Beginner");
            }
            
            return dto;
        });
        
        return ResponseEntity.ok(dtoPage);
    }
    
    /**
     * Get a user's rank information.
     * 
     * @param userId The ID of the user.
     * @return The user's leaderboard entry.
     */
    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user's rank", 
              description = "Retrieve a user's position on the leaderboard")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(schema = @Schema(implementation = LeaderboardEntryDTO.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<LeaderboardEntryDTO> getUserRank(
            @Parameter(description = "User ID", example = "user-12345", required = true)
            @PathVariable String userId) {
        
        Leaderboard leaderboard = leaderboardService.getUserRank(userId);
        
        if (leaderboard == null) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }
        
        LeaderboardEntryDTO dto = leaderboardMapper.toDTO(leaderboard);
        return ResponseEntity.ok(dto);
    }
    
    /**
     * Get the top N users on the leaderboard.
     * 
     * @param limit The number of top users to retrieve.
     * @return A list of the top N leaderboard entries.
     */
    @GetMapping("/top")
    @Operation(summary = "Get top users", 
              description = "Retrieve the top N users on the leaderboard")
    @ApiResponse(responseCode = "200", description = "Success",
                content = @Content(schema = @Schema(implementation = List.class)))
    public ResponseEntity<List<LeaderboardEntryDTO>> getTopUsers(
            @Parameter(description = "Number of top users to retrieve", example = "5")
            @RequestParam(defaultValue = "5") int limit) {
        
        List<Leaderboard> topUsers = leaderboardService.getTopUsers(limit);
        List<LeaderboardEntryDTO> dtos = leaderboardMapper.toDTOList(topUsers);
        
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * Recalculate all ranks on the leaderboard.
     * 
     * @return The number of leaderboard entries updated.
     */
    @GetMapping("/recalculate")
    @Operation(summary = "Recalculate ranks", 
              description = "Recalculate all ranks on the leaderboard")
    @ApiResponse(responseCode = "200", description = "Success",
                content = @Content(schema = @Schema(implementation = Integer.class)))
    public ResponseEntity<Integer> recalculateRanks() {
        int updatedCount = leaderboardService.calculateRanks();
        return ResponseEntity.ok(updatedCount);
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
