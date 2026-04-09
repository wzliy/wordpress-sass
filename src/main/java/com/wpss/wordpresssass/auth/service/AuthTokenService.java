package com.wpss.wordpresssass.auth.service;

import com.wpss.wordpresssass.auth.config.AuthProperties;
import com.wpss.wordpresssass.common.auth.CurrentUser;
import com.wpss.wordpresssass.common.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Date;

@Service
public class AuthTokenService {

    private final AuthProperties authProperties;

    public AuthTokenService(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    public String createToken(CurrentUser currentUser) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(authProperties.getExpireSeconds());
        return Jwts.builder()
                .issuer(authProperties.getIssuer())
                .subject(currentUser.username())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claim("userId", currentUser.userId())
                .claim("tenantId", currentUser.tenantId())
                .claim("username", currentUser.username())
                .signWith(signingKey(), Jwts.SIG.HS256)
                .compact();
    }

    public CurrentUser parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey())
                    .requireIssuer(authProperties.getIssuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return new CurrentUser(
                    readLongClaim(claims, "userId"),
                    readLongClaim(claims, "tenantId"),
                    readStringClaim(claims, "username")
            );
        } catch (ExpiredJwtException ex) {
            throw new UnauthorizedException("Token expired");
        } catch (JwtException | IllegalArgumentException ex) {
            throw new UnauthorizedException("Invalid token");
        }
    }

    public long expireSeconds() {
        return authProperties.getExpireSeconds();
    }

    private SecretKey signingKey() {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(authProperties.getSecret().getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to initialize JWT signing key", ex);
        }
    }

    private Long readLongClaim(Claims claims, String key) {
        Object value = claims.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new UnauthorizedException("Invalid token");
    }

    private String readStringClaim(Claims claims, String key) {
        Object value = claims.get(key);
        if (value instanceof String text && !text.isBlank()) {
            return text;
        }
        throw new UnauthorizedException("Invalid token");
    }
}
