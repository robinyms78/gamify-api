// Java class for RewardNotFoundException
// RewardNotFoundException.java

package sg.edu.ntu.gamify_demo.exceptions;

public class RewardNotFoundException extends RuntimeException {
    public RewardNotFoundException(Long id) {
        super("Could not find reward with id: " + id);
    }
}
