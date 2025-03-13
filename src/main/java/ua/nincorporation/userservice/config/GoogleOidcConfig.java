package ua.nincorporation.userservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class GoogleOidcConfig {

    @Value("${app.google.issuer-uri:https://accounts.google.com}")
    private String issuer;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Bean
    @Qualifier("googleIdTokenDecoder")
    public JwtDecoder googleIdTokenDecoder() {
        NimbusJwtDecoder dec = (NimbusJwtDecoder) JwtDecoders.fromOidcIssuerLocation(issuer);

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> withAudience = jwt ->
                jwt.getAudience().contains(clientId)
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "aud mismatch", null));
        OAuth2TokenValidator<Jwt> withTimestamps = new JwtTimestampValidator(Duration.ofSeconds(60));

        dec.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience, withTimestamps));
        return dec;
    }
}