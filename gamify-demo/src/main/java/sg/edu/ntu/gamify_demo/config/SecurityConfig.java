package sg.edu.ntu.gamify_demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // @Bean
    // @Profile("!test")
    // public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    //     http
    //         .csrf(csrf -> csrf.disable())
    //         .headers(headers -> headers
    //             .contentSecurityPolicy(csp -> csp
    //                 .policyDirectives("default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; font-src 'self'")
    //             )
    //             .frameOptions(frame -> frame.deny())
    //             .xssProtection(xss -> xss
    //                 .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
    //             )
    //             .contentTypeOptions()
    //         )
    //         .authorizeHttpRequests(auth -> auth
    //             .requestMatchers(
    //                 "/v3/api-docs/**",
    //                 "/swagger-ui/**",
    //                 "/swagger-ui.html",
    //                 "/swagger-ui/index.html",
    //                 "/swagger-ui/swagger-initializer.js",
    //                 "/swagger-ui/swagger-ui.css",
    //                 "/swagger-ui/swagger-ui-bundle.js",
    //                 "/swagger-ui/swagger-ui-standalone-preset.js",
    //                 "/swagger-ui/favicon-32x32.png",
    //                 "/swagger-ui/favicon-16x16.png",
    //                 "/error/**",
    //                 "/auth/register",
    //                 "/auth/login",
    //                 "/api/debug/**",
    //                 "/debug/**"
    //             ).permitAll()
    //             .anyRequest().authenticated()
    //         )
    //         .sessionManagement(session -> session
    //             .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    //         )
    //         .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    //     return http.build();
    // }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain customSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())  // If CSRF is enabled, make sure the request includes a CSRF token
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()  
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