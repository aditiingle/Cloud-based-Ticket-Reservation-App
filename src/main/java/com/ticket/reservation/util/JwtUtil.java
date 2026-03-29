package com.ticket.reservation.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ticket.reservation.model.Administrator;
import com.ticket.reservation.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret:defaultSecretKeyThatShouldBeAtLeast256BitsLongForSecurity}")
    private String secret;
    
    @Value("${jwt.expiration:3600000}") // Default 1 hour in milliseconds
    private Long expiration;
    
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    public String generateToken(String userId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        return createToken(claims, email);
    }
    
    /**
     * Generates a JWT token with role information based on user type.
     * @param user The user object (Administrator or Customer)
     * @return JWT token string
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        String email = user.getEmail() != null ? user.getEmail() : user.getPhone();
        claims.put("email", email);
        
        // Add role based on user type
        if (user instanceof Administrator) {
            claims.put("role", "ROLE_ADMIN");
        } else {
            claims.put("role", "ROLE_CUSTOMER");
        }
        
        return createToken(claims, email);
    }
    
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }
    
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }
    
    /**
     * Extracts the role from the JWT token.
     * @param token The JWT token string
     * @return The role string (e.g., "ROLE_ADMIN" or "ROLE_CUSTOMER")
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }
    
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    public Boolean validateToken(String token, String email) {
        final String extractedEmail = extractEmail(token);
        return (extractedEmail.equals(email) && !isTokenExpired(token));
    }
    
    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
