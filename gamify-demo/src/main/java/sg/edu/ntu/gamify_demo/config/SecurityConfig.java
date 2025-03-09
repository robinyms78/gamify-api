package sg.edu.ntu.gamify_demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security configuration for the application.
 * Provides beans for password encoding and other security-related functionality.
 */
@Configuration
public class SecurityConfig {

    /**
     * Creates a password encoder bean for securely hashing passwords.
     * 
     * @return A BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
