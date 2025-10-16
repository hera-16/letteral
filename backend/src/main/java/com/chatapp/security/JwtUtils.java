package com.chatapp.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private int jwtExpirationMs;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    public String generateJwtToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }
    
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    
    public boolean validateJwtToken(String authToken) {
        try {
            System.out.println("🔍 Validating JWT | Length: " + authToken.length() + " | First 30 chars: " + authToken.substring(0, Math.min(30, authToken.length())));
            System.out.println("🔑 JWT Secret length: " + jwtSecret.length() + " | First 10 chars: " + jwtSecret.substring(0, Math.min(10, jwtSecret.length())));
            
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(authToken);
            
            System.out.println("✅ JWT validation successful");
            return true;
        } catch (SecurityException e) {
            System.err.println("❌ Invalid JWT signature: " + e.getMessage());
            System.err.println("   This usually means the JWT_SECRET environment variable doesn't match");
        } catch (MalformedJwtException e) {
            System.err.println("❌ Invalid JWT token (malformed): " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.err.println("❌ JWT token is expired: " + e.getMessage() + " | Expired at: " + e.getClaims().getExpiration());
        } catch (UnsupportedJwtException e) {
            System.err.println("❌ JWT token is unsupported: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("❌ JWT claims string is empty: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ JWT validation error: " + e.getClass().getName() + " | " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
}