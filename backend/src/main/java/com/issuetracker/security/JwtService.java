package com.issuetracker.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

// JwtService is responsible for creating and validating JWT tokens.
// A JWT has three parts: header.payload.signature
//   header  → algorithm used (HS512)
//   payload → claims: who the user is, when the token was issued, when it expires
//   signature → HMAC of header+payload using our secret key — prevents tampering
@Service
public class JwtService {

    // Loaded from application.properties — never hardcode secrets in source code
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}")
    private long expirationMs; // default 24 hours in milliseconds

    // Generate a JWT token for a user after successful login
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())   // "sub" claim = the user's email
                .issuedAt(new Date())                 // "iat" = when issued
                .expiration(new Date(System.currentTimeMillis() + expirationMs)) // "exp"
                .signWith(getSigningKey())             // HMAC-SHA512 signature
                .compact();                            // produces "header.payload.signature"
    }

    // Extract the user's email from a token (reads the "sub" claim)
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Check if the token is valid: email matches the user and token hasn't expired
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }

    // Build the HMAC signing key from the secret string
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
}
