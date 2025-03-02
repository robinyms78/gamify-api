package sg.edu.ntu.gamify_demo.Services;

public class RewardNotFoundException extends RuntimeException {
    RewardNotFoundException(String id) {
        super("Could not find reward with id: " + id);
    }
}
