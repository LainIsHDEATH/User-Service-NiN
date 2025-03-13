package ua.nincorporation.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OauthCodeRequest (@JsonProperty("code") String code,
                                @JsonProperty("code_verifier") String codeVerifier) {
}
