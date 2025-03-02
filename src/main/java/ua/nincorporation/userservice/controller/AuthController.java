package ua.nincorporation.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.nincorporation.userservice.dto.AuthRequestDTO;
import ua.nincorporation.userservice.dto.AuthResponseDto;
import ua.nincorporation.userservice.dto.UserCreateDto;
import ua.nincorporation.userservice.model.User;
import ua.nincorporation.userservice.security.CustomUserDetails;
import ua.nincorporation.userservice.security.JWTUtil;
import ua.nincorporation.userservice.service.AuthenticationService;
import ua.nincorporation.userservice.service.UserService;
import ua.nincorporation.userservice.util.UserValidator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationService authenticationService;
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@RequestBody @Valid UserCreateDto userCreateDto) {
        log.debug("Register attempt for username = {}", userCreateDto.username());

        User user = authenticationService.register(userCreateDto);

        List<String> roles = List.of(user.getRole().name());

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roles);

        AuthResponseDto body = new AuthResponseDto(token, jwtUtil.getExpirationSeconds());
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(body);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponseDto> authenticate(@RequestBody @Valid AuthRequestDTO authRequestDTO) {
        log.debug("Authenticate attempt for username = {}", authRequestDTO.username());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequestDTO.username(), authRequestDTO.password())
        );
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        String token = jwtUtil.generateToken(userDetails.getId(), userDetails.getUsername(), roles);

        AuthResponseDto body = new AuthResponseDto(token, jwtUtil.getExpirationSeconds());
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        return ResponseEntity.ok().headers(headers).body(body);
    }
}
