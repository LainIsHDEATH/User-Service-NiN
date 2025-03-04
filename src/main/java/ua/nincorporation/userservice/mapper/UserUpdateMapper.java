package ua.nincorporation.userservice.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ua.nincorporation.userservice.dto.UserUpdateDTO;
import ua.nincorporation.userservice.model.User;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public final class UserUpdateMapper {

    private final PasswordEncoder passwordEncoder;

    public User update(User user, UserUpdateDTO updatedUser) {
        if (user == null) return null;
        if (updatedUser == null) return user;

        Optional.ofNullable(updatedUser.username())
                .filter(s -> !s.isBlank())
                .ifPresent(user::setUsername);

        Optional.ofNullable(updatedUser.password())
                .filter(s -> !s.isBlank())
                .map(passwordEncoder::encode)
                .ifPresent(user::setPassword);

        Optional.ofNullable(updatedUser.email())
                .filter(s -> !s.isBlank())
                .ifPresent(user::setEmail);

        Optional.ofNullable(updatedUser.dateOfBirth())
                .ifPresent(user::setDateOfBirth);
        return user;
    }
}
