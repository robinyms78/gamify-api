package sg.edu.ntu.gamify_demo;

import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Base64;

/**
 * A simple test program to verify that the JWT_SECRET can be properly decoded and used.
 */
public class JwtSecretTest {

    public static void main(String[] args) {
        // The JWT_SECRET from the environment variable
        String secret = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";
        
        System.out.println("Testing JWT_SECRET: " + secret);
        
        try {
            // Test if the secret is a hexadecimal string
            if (isHexString(secret)) {
                System.out.println("Secret is a valid hexadecimal string");
                
                // Convert hex string to bytes
                byte[] keyBytes = hexStringToByteArray(secret);
                System.out.println("Converted to byte array of length: " + keyBytes.length);
                
                // Create a key from the bytes
                Key key = Keys.hmacShaKeyFor(keyBytes);
                System.out.println("Successfully created key: " + key.getAlgorithm());
                
                // Print the key in Base64 for reference
                String base64Key = Base64.getEncoder().encodeToString(keyBytes);
                System.out.println("Base64 encoded key: " + base64Key);
            } else {
                System.out.println("Secret is not a valid hexadecimal string");
                
                // Try to use the secret directly as bytes
                Key key = Keys.hmacShaKeyFor(secret.getBytes());
                System.out.println("Successfully created key using raw bytes: " + key.getAlgorithm());
            }
            
            System.out.println("JWT_SECRET test passed!");
        } catch (Exception e) {
            System.out.println("JWT_SECRET test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Checks if a string is a valid hexadecimal string.
     * 
     * @param str The string to check
     * @return true if the string is a valid hexadecimal string, false otherwise
     */
    private static boolean isHexString(String str) {
        return str.matches("^[0-9A-Fa-f]+$");
    }
    
    /**
     * Converts a hexadecimal string to a byte array.
     * 
     * @param hexString The hexadecimal string to convert
     * @return The byte array
     */
    private static byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }
}
