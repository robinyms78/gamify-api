package sg.edu.ntu.gamify_demo.exceptions;

public class RewardNotFoundException extends RuntimeException {
    public RewardNotFoundException(String id) {
        super("Could not find reward with id: " + id);
    }
}
