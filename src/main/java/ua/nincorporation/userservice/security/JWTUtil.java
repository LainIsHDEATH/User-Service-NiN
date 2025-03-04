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
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Component
public class JWTUtil {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLES = "roles";

    private final String issuer;
    private final Duration expiration;

    private final Algorithm algorithm;
    @Getter
    private final JWTVerifier verifier;

    public JWTUtil(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.secret-base64:false}") boolean secretIsBase64,
            @Value("${jwt.issuer:NiN}") String issuer,
            @Value("${jwt.expiration-minutes:60}") long expiresMinutes,
            @Value("${jwt.leeway-seconds:60}") long leewaySeconds
    ) {
        this.issuer = issuer;
        this.expiration = Duration.ofMinutes(expiresMinutes);

        byte[] secretBytes = secretIsBase64
                ? Base64.getDecoder().decode(secretKey)
                : secretKey.getBytes(StandardCharsets.UTF_8);

        this.algorithm = Algorithm.HMAC256(secretBytes);

        this.verifier = JWT.require(algorithm)
                .withIssuer(this.issuer)
                .acceptLeeway(leewaySeconds)
                .build();
    }

    public String generateToken(Long userId, String username, Collection<String> roles) {
        Instant now = Instant.now();
        Instant issuedAt = Instant.from(now);
        Instant expiresAt = Instant.from(now.plus(expiration));

        var builder = JWT.create()
                .withSubject(userId == null ? "" : String.valueOf(userId))
                .withIssuer(issuer)
                .withIssuedAt(issuedAt)
                .withExpiresAt(expiresAt)
                .withClaim(CLAIM_USER_ID, userId)
                .withClaim(CLAIM_USERNAME, username);

        if (roles != null && !roles.isEmpty()) {
            builder.withArrayClaim(CLAIM_ROLES, roles.toArray(new String[0]));
        }

        return builder.sign(algorithm);
    }

    public String generateToken(Long userId, String username) {
        return generateToken(userId, username, Collections.emptyList());
    }

    public Optional<Long> tryExtractUserId(String token) {
        try {
            DecodedJWT decoded = verifier.verify(token);
            return Optional.ofNullable(decoded.getClaim(CLAIM_USER_ID).asLong());
        } catch (JWTVerificationException e) {
            log.debug("Failed to verify JWT when extracting userId: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<String> tryExtractUsername(String token) {
        try {
            DecodedJWT decoded = verifier.verify(token);
            return Optional.ofNullable(decoded.getClaim(CLAIM_USERNAME).asString());
        } catch (JWTVerificationException e) {
            log.debug("Failed to verify JWT when extracting username: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public List<String> tryExtractRoles(String token) {
        try {
            DecodedJWT decoded = verifier.verify(token);
            String[] roles = decoded.getClaim(CLAIM_ROLES).asArray(String.class);
            if (roles == null) return Collections.emptyList();
            return Arrays.asList(roles);
        } catch (JWTVerificationException e) {
            log.debug("Failed to verify JWT when extracting roles: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            DecodedJWT decodedJWT = verifier.verify(token);

            String username = decodedJWT.getClaim(CLAIM_USERNAME).asString();

            Date expires = decodedJWT.getExpiresAt();
            boolean notExpired = expires != null && expires.after(Date.from(Instant.now()));

            return username != null && username.equals(userDetails.getUsername())
                    && notExpired;
        } catch (JWTVerificationException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Long getExpirationSeconds() {
        return expiration.getSeconds();
    }

    public static class JwtValidationException extends RuntimeException {
        public JwtValidationException(String message) { super(message); }
        public JwtValidationException(String message, Throwable cause) { super(message, cause); }
    }
}
