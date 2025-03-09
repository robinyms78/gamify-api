package sg.edu.ntu.gamify_demo.interfaces;

import sg.edu.ntu.gamify_demo.dtos.LadderStatusDTO;

/**
 * Service interface for ladder status operations.
 * Provides methods for retrieving and updating user ladder status.
 */
public interface LadderStatusService {
    
    /**
     * Get a user's current ladder status.
     * 
     * @param userId The ID of the user.
     * @return The user's ladder status as a DTO, or null if the user doesn't exist.
     */
    LadderStatusDTO getUserLadderStatus(String userId);
    
    /**
     * Update a user's ladder status based on their earned points.
     * 
     * @param userId The ID of the user.
     * @return The updated ladder status as a DTO, or null if the user doesn't exist.
     */
    LadderStatusDTO updateUserLadderStatus(String userId);
}
