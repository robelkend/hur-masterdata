package com.rsoft.hurmanagement.hurmasterdata.security;

import com.rsoft.hurmanagement.hurmasterdata.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final JwtProperties properties;
    private final Key signingKey;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String username, boolean passwordExpired) {
        Instant now = Instant.now();
        Instant exp = now.plus(properties.getAccessTokenExpirationMinutes(), ChronoUnit.MINUTES);
        return Jwts.builder()
                .setSubject(username)
                .claim("pwd_expired", passwordExpired)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateAccessToken(String username) {
        return generateAccessToken(username, false);
    }

    public String getUsernameFromToken(String token) {
        return getAllClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            getAllClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean isPasswordExpired(String token) {
        Claims claims = getAllClaims(token);
        Object flag = claims.get("pwd_expired");
        if (flag instanceof Boolean) {
            return (Boolean) flag;
        }
        if (flag instanceof String) {
            return Boolean.parseBoolean((String) flag);
        }
        return false;
    }

    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
