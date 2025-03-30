package sg.edu.ntu.gamify_demo.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import jakarta.annotation.PostConstruct;
import sg.edu.ntu.gamify_demo.models.LadderLevel;
import sg.edu.ntu.gamify_demo.repositories.LadderLevelRepository;

/**
 * Test security configuration for integration tests.
 * Disables security for test contexts to allow tests to run without authentication.
 */
@TestConfiguration
@EnableWebSecurity
public class TestIntegrationConfig {

    /**
     * Bean to initialize ladder levels for tests.
     * Creates default ladder levels if they don't exist.
     * 
     * @param ladderLevelRepository The repository for ladder levels
     * @return A bean that initializes ladder levels
     */
    @Bean
    public LadderLevelInitializer ladderLevelInitializer(LadderLevelRepository ladderLevelRepository) {
        return new LadderLevelInitializer(ladderLevelRepository);
    }
    
    /**
     * Helper class to initialize ladder levels for tests.
     */
    public static class LadderLevelInitializer {
        
        private final LadderLevelRepository ladderLevelRepository;
        
        public LadderLevelInitializer(LadderLevelRepository ladderLevelRepository) {
            this.ladderLevelRepository = ladderLevelRepository;
        }
        
        @PostConstruct
        public void initLadderLevels() {
            // Create default ladder levels if they don't exist
            if (ladderLevelRepository.count() == 0) {
                ladderLevelRepository.save(new LadderLevel(1L, "Beginner", 0L));
                ladderLevelRepository.save(new LadderLevel(2L, "Intermediate", 100L));
                ladderLevelRepository.save(new LadderLevel(3L, "Advanced", 300L));
                ladderLevelRepository.save(new LadderLevel(4L, "Expert", 600L));
                ladderLevelRepository.save(new LadderLevel(5L, "Master", 1000L));
            }
        }
    }

    /**
     * Configures the security filter chain for integration tests.
     * Disables security features that might interfere with tests.
     * 
     * @param http The HttpSecurity to configure
     * @return The configured SecurityFilterChain
     * @throws Exception If an error occurs during configuration
     */
    @Bean(name = "testSecurityFilterChain")
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; font-src 'self'")
                )
                .frameOptions(frame -> frame.deny())
                .xssProtection(xss -> xss
                    .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                )
                .contentTypeOptions()
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(new AntPathRequestMatcher("/**")).permitAll()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .securityMatcher("/**");
        
        return http.build();
    }
}
