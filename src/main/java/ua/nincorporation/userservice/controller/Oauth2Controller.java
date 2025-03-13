package ua.nincorporation.userservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import ua.nincorporation.userservice.dto.AuthResponseDto;
import ua.nincorporation.userservice.dto.GoogleTokenResponse;
import ua.nincorporation.userservice.dto.OauthCodeRequest;
import ua.nincorporation.userservice.model.User;
import ua.nincorporation.userservice.security.JWTUtil;
import ua.nincorporation.userservice.service.OidcLoginService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class Oauth2Controller {

    private final JWTUtil jwtUtil;
    private final OidcLoginService oauthLoginService;
    private final RestClient restClient;

    @Qualifier("googleIdTokenDecoder")
    private final JwtDecoder googleIdTokenDecoder;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;

    @PostMapping("/exchange")
    public ResponseEntity<AuthResponseDto> exchangeCodeForToken(@RequestBody OauthCodeRequest oauthCodeRequest) {
        String code = oauthCodeRequest.code();
        String codeVerifier = oauthCodeRequest.codeVerifier();

        GoogleTokenResponse tokenResp = exchangeCodeWithRestClient(restClient, code, codeVerifier);

        if (tokenResp == null || tokenResp.idToken() == null) {
            throw new RuntimeException("No id_token in token response: " + tokenResp);
        }

        String idToken = tokenResp.idToken();

        Jwt jwt = googleIdTokenDecoder.decode(idToken);

        User user = oauthLoginService.findOrCreateByGoogleId(jwt);

        List<String> roles = List.of(user.getRole().name());

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roles);

        AuthResponseDto body2 = new AuthResponseDto(token, jwtUtil.getExpirationSeconds());
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(body2);
    }

    public GoogleTokenResponse exchangeCodeWithRestClient(
            RestClient rc,
            String code,
            String codeVerifier
    ) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("client_id", googleClientId);
        form.add("client_secret", googleClientSecret);
        form.add("redirect_uri", googleRedirectUri);
        form.add("code_verifier", codeVerifier);

        return rc.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(form)
                .retrieve()
                .body(GoogleTokenResponse.class);
    }
}
