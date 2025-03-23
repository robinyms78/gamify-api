package sg.edu.ntu.gamify_demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

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

    @Bean
    public SecurityFilterChain customSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())  // If CSRF is enabled, make sure the request includes a CSRF token
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers(HttpMethod.POST, "/api/user").permitAll()                  // User management 
                                .requestMatchers(HttpMethod.POST, "/api/gamification/users").permitAll()    // Gamification
                                .requestMatchers(HttpMethod.POST, "/tasks/events").permitAll()              // Tasks events
                                .requestMatchers(HttpMethod.POST, "/rewards").permitAll()                   // Rewards
                                .requestMatchers(HttpMethod.POST, "/rewards/redemptions").permitAll()       // Rewards redemptions
                                .requestMatchers(HttpMethod.POST, "/rewards/redeem").permitAll()            // User redeem rewards
                                .requestMatchers(HttpMethod.POST, "/api/achievements").permitAll()          // User achievements
                                .requestMatchers(HttpMethod.POST, "/api/achievements/process").permitAll()  // Process user achievement events
                                .requestMatchers(HttpMethod.POST, "/api/ladder/users").permitAll()          // Ladder system
                                .requestMatchers(HttpMethod.POST, "/api/ladder/levels").permitAll()         // Post ladder levels
                                .anyRequest().permitAll()
                )
                .build();
    }

}