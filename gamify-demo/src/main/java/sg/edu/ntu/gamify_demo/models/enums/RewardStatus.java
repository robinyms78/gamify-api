package sg.edu.ntu.gamify_demo.models.enums;

/**
 * Enum representing the possible statuses of a reward redemption.
 * Corresponds to the 'reward_status' ENUM type in the database.
 */
public enum RewardStatus {
    PENDING("pending"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    private final String value;

    RewardStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RewardStatus fromValue(String value) {
        for (RewardStatus status : RewardStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown reward status: " + value);
    }
}
