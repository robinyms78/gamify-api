package sg.edu.ntu.gamify_demo.services;

import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import sg.edu.ntu.gamify_demo.models.User;

@Service
public class AuthenticationService {
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration.ms}")
    private long expirationMs;
    
    private Key key;

    @PostConstruct
    public void init() {
        // Check if the secret is a hexadecimal string
        if (isHexString(secret)) {
            // Convert hex string to bytes
            byte[] keyBytes = hexStringToByteArray(secret);
            this.key = Keys.hmacShaKeyFor(keyBytes);
        } else {
            // Use the secret directly as bytes
            this.key = Keys.hmacShaKeyFor(secret.getBytes());
        }
    }

    /**
     * Checks if a string is a valid hexadecimal string.
     * 
     * @param str The string to check
     * @return true if the string is a valid hexadecimal string, false otherwise
     */
    private boolean isHexString(String str) {
        return str.matches("^[0-9A-Fa-f]+$");
    }

    /**
     * Converts a hexadecimal string to a byte array.
     * 
     * @param hexString The hexadecimal string to convert
     * @return The byte array
     */
    private byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}
