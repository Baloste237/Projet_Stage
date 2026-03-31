package com.example.backend.Security.service.implementation;

import com.example.backend.Security.service.JWTService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTServiceImpl implements JWTService {

    // Clé fixe d'au moins 32 caractères pour HS256
    private static final String SECRET_KEY = "mysecretkeymysecretkeymysecretkey12345";

    @Override
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();

        return Jwts.builder()
                .claims(claims)                   // ⚡ claims() dans 0.12.x
                .subject(username)                // ⚡ subject() au lieu de setSubject()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(getKey())
                .compact();
    }

    @Override
    public String extractUserName(String jwtToken) {
        return extractClaim(jwtToken, Claims::getSubject);
    }

    @Override
    public boolean validateToken(String jwtToken, UserDetails userDetails) {
        final String username = extractUserName(jwtToken);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(jwtToken);
    }

    private boolean isTokenExpired(String jwtToken) {
        return extractClaim(jwtToken, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String jwtToken, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(jwtToken);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String jwtToken) {
        return Jwts.parser()                  // ⚡ parser() au lieu de parserBuilder()
                .verifyWith(getKey())         // ⚡ verifyWith() au lieu de setSigningKey()
                .build()
                .parseSignedClaims(jwtToken)  // ⚡ parseSignedClaims() au lieu de parseClaimsJws()
                .getPayload();                // ⚡ getPayload() au lieu de getBody()
    }

    private SecretKey getKey() {
        byte[] keyBytes = SECRET_KEY.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}