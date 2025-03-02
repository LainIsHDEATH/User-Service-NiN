package ua.nincorporation.userservice.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
public class JWTUtil {

    private final String issuer;
    private final String subject;
    private final long expiresMinutes;

    private final Algorithm algorithm;
    @Getter
    private final JWTVerifier verifier;

    public JWTUtil(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.secret-base64:false}") boolean secretIsBase64,
            @Value("${jwt.issuer:NiN}") String issuer,
            @Value("${jwt.subject:User details}") String subject,
            @Value("${jwt.expiration-minutes:60}") long expiresMinutes,
            @Value("${jwt.leeway-seconds:60}") long leewaySeconds
    ) {
        this.issuer = issuer;
        this.subject = subject;
        this.expiresMinutes = expiresMinutes;

        byte[] secretBytes = secretIsBase64
                ? Base64.getDecoder().decode(secretKey)
                : secretKey.getBytes(StandardCharsets.UTF_8);

        this.algorithm = Algorithm.HMAC256(secretBytes);

        this.verifier = JWT.require(algorithm)
                .withIssuer(this.issuer)
                .withSubject(this.subject)
                .acceptLeeway(leewaySeconds)
                .build();
    }

    public String generateToken(Long userId, String username) {
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiresAt = Date.from(now.plus(expiresMinutes, ChronoUnit.MINUTES));

        return JWT.create()
                .withSubject(subject)
                .withIssuer(issuer)
                .withIssuedAt(issuedAt)
                .withExpiresAt(expiresAt)
                .withClaim("userId", userId)
                .withClaim("username", username)
                .sign(algorithm);
    }

    public String generateToken(Long userId, String username, Collection<String> roles) {
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiresAt = Date.from(now.plus(expiresMinutes, ChronoUnit.MINUTES));

        var builder = JWT.create()
                .withSubject(subject)
                .withIssuer(issuer)
                .withIssuedAt(issuedAt)
                .withExpiresAt(expiresAt)
                .withClaim("userId", userId)
                .withClaim("username", username);

        if (roles != null && !roles.isEmpty()) {
            builder.withArrayClaim("role", roles.toArray(new String[0]));
        }

        return builder.sign(algorithm);
    }

    public Optional<String> tryExtractUsername(String token) {
        try {
            DecodedJWT decoded = verifier.verify(token);
            return Optional.ofNullable(decoded.getClaim("username").asString());
        } catch (JWTVerificationException e) {
            log.debug("Failed to verify JWT when extracting username: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<Long> tryExtractUserId(String token) {
        try {
            DecodedJWT decoded = verifier.verify(token);
            return Optional.ofNullable(decoded.getClaim("userId").asLong());
        } catch (JWTVerificationException e) {
            log.debug("Failed to verify JWT when extracting userId: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public String extractUsername(String token) {
        return tryExtractUsername(token)
                .orElseThrow(() -> new JwtValidationException("Invalid or expired JWT token"));
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            DecodedJWT decodedJWT = verifier.verify(token);

            String username = decodedJWT.getClaim("username").asString();

            Date expires = decodedJWT.getExpiresAt();
            boolean notExpired = expires != null && expires.after(Date.from(Instant.now()));

            return username != null && username.equals(userDetails.getUsername()) && notExpired;
        } catch (JWTVerificationException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Long getExpirationSeconds() {
        return expiresMinutes;
    }

    public static class JwtValidationException extends RuntimeException {
        public JwtValidationException(String message) { super(message); }
        public JwtValidationException(String message, Throwable cause) { super(message, cause); }
    }
}
