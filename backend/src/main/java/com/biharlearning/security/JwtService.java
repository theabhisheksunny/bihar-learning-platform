package com.biharlearning.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expiryMinutes;

    public JwtService(@Value("${app.security.jwt.secret}") String secret,
                      @Value("${app.security.jwt.expiry-minutes:120}") long expiryMinutes) {
        byte[] decoded = Base64.getDecoder().decode(secret);
        this.key = Keys.hmacShaKeyFor(decoded);
        this.expiryMinutes = expiryMinutes;
    }

    public String generate(String subject, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiryMinutes * 60_000);
        return Jwts.builder()
                .subject(subject)
                .claim("role", role)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
