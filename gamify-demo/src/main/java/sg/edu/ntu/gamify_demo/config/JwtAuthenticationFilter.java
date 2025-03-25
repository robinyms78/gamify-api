package sg.edu.ntu.gamify_demo.config;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sg.edu.ntu.gamify_demo.models.User;
import sg.edu.ntu.gamify_demo.repositories.UserRepository;
import sg.edu.ntu.gamify_demo.services.AuthenticationService;

/**
 * Filter for JWT authentication.
 * Extracts and validates JWT tokens from requests and sets the authentication context.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationService authService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(AuthenticationService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Extract Authorization header
        String authHeader = request.getHeader("Authorization");
        
        // Check if Authorization header is present and has the correct format
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Extract token
            String token = authHeader.substring(7);
            
            // Validate token
            if (authService.validateToken(token)) {
                // Extract username from token
                String username = authService.getUsernameFromToken(token);
                
                // Find user by username
                Optional<User> optionalUser = userRepository.findByUsername(username);
                
                if (optionalUser.isPresent()) {
                    User user = optionalUser.get();
                    
                    // Create authentication token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                    );
                    
                    // Set authentication details
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }
        
        // Continue filter chain
        filterChain.doFilter(request, response);
    }
}
