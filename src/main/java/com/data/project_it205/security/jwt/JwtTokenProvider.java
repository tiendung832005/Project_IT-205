package com.data.project_it205.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    private final String JWT_SECRET_STRING = "IT205_project_secret_key_very_long_and_secure_for_jwt_token_generation";
    private final SecretKey JWT_SECRET = Keys.hmacShaKeyFor(JWT_SECRET_STRING.getBytes());
    private final long JWT_EXPIRATION = 86400000L; // 1 ngày

    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION);

        String token = Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(JWT_SECRET)
                .compact();

        log.info("Generated token for user: {} with role: {}", username, role);
        return token;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(JWT_SECRET)
                    .build()
                    .parseClaimsJws(token);
            log.info("Token validation successful");
            return true;
        } catch (Exception ex) {
            log.warn("Token validation failed: {}", ex.getMessage());
            return false;
        }
    }

    public String getUsernameFromJWT(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(JWT_SECRET)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            String username = claims.getSubject();
            log.info("Extracted username from token: {}", username);
            return username;
        } catch (Exception ex) {
            log.error("Error extracting username from token: {}", ex.getMessage());
            throw ex;
        }
    }
}