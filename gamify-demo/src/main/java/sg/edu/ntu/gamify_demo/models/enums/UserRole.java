package sg.edu.ntu.gamify_demo.models.enums;

/**
 * Enum representing the possible roles a user can have in the system.
 * Corresponds to the 'user_role' ENUM type in the database.
 */
public enum UserRole {
    EMPLOYEE("employee"),
    MANAGER("manager"),
    HR_ADMIN("hr_admin");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UserRole fromValue(String value) {
        for (UserRole role : UserRole.values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown user role: " + value);
    }
}
