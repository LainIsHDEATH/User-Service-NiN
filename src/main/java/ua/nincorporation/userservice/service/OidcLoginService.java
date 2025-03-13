package ua.nincorporation.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.nincorporation.userservice.model.Role;
import ua.nincorporation.userservice.model.User;
import ua.nincorporation.userservice.model.UserProvider;
import ua.nincorporation.userservice.repository.UserProviderRepository;
import ua.nincorporation.userservice.repository.UserRepository;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OidcLoginService {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;
    private final UserProviderRepository providerRepository;

    @Retryable(
            retryFor = DataIntegrityViolationException.class,
            maxAttempts = 2,
            backoff = @Backoff(delay = 500)
    )
    @Transactional
    public User findOrCreateByGoogleId(Jwt googleIdToken) {
        String provider = "google";
        String sub = googleIdToken.getSubject();
        String email = googleIdToken.getClaimAsString("email");
        String name = googleIdToken.getClaimAsString("name");
        Boolean emailVerified = googleIdToken.getClaim("email_verified");

        if (email == null || Boolean.FALSE.equals(emailVerified)) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token", "email not verified", null));
        }

        var linked = providerRepository.findByProviderAndProviderUserId(provider, sub)
                .map(UserProvider::getUser);
        if (linked.isPresent()) return linked.get();

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setEmail(email);
            u.setUsername(name);
            u.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            u.setDateOfBirth(LocalDate.of(1990, 1, 1));
            u.setRole(Role.USER);
            return userRepository.saveAndFlush(u);
        });

        UserProvider up = new UserProvider();
        up.setUser(user);
        up.setProvider(provider);
        up.setProviderUserId(sub);
        providerRepository.saveAndFlush(up);

        return user;
    }
}
