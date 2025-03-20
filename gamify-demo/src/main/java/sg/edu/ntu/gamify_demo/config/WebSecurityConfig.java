package sg.edu.ntu.gamify_demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Web security configuration for the application.
 * Configures security settings, authentication, and authorization rules.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    /**
     * Configures the security filter chain.
     * Only active when not in test profile to avoid conflicts with test security config.
     * 
     * @param http The HttpSecurity to configure
     * @return The configured SecurityFilterChain
     * @throws Exception If an error occurs during configuration
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    public WebSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    @Profile("!test") // Only active when not in test profile
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for API endpoints
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/swagger-ui/index.html",
                    "/swagger-ui/swagger-initializer.js",
                    "/swagger-ui/swagger-ui.css",
                    "/swagger-ui/swagger-ui-bundle.js",
                    "/swagger-ui/swagger-ui-standalone-preset.js",
                    "/swagger-ui/favicon-32x32.png",
                    "/swagger-ui/favicon-16x16.png",
                    "/error/**",
                    "/auth/register",
                    "/auth/login",
                    "/api/debug/**",  // Allow access to debug endpoints
                    "/debug/**"       // Allow access to new debug endpoints
                ).permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                // Use stateless session management for REST APIs
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // Add JWT authentication filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
