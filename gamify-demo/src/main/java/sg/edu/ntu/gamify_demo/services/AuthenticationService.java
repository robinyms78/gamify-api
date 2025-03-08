// Java class for AuthenticationService
// AuthenticationService.java

package sg.edu.ntu.gamify_demo.services;

public class AuthenticationService {
    private String userName;
    private String password;
    private String token;

    // Constructor
    public AuthenticationService() {
    }

    // Methods
    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}