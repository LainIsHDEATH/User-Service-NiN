package ua.nincorporation.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("id_token")     String idToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("token_type")   String tokenType,
        @JsonProperty("expires_in")   Long   expiresIn,
        @JsonProperty("scope") String scope,
        @JsonProperty("error")            String error,
        @JsonProperty("error_description") String errorDescription
) {}
