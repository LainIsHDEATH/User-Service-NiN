package ua.nincorporation.userservice.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ua.nincorporation.userservice.dto.UserCreateDto;
import ua.nincorporation.userservice.dto.UserUpdateDTO;
import ua.nincorporation.userservice.model.User;
import ua.nincorporation.userservice.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class UserValidator implements Validator {
    private final UserRepository userRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.equals(clazz)
                || UserCreateDto.class.equals(clazz)
                || UserUpdateDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        User user = (User) target;

        if (userRepository.existsByUsername(user.getUsername())) {
            errors.rejectValue("username", "", "User with such username already exists.");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            errors.rejectValue("email", "", "User with such email already exists.");
        }
    }

    public boolean isValid(User user) {
        return !userRepository.existsByUsername(user.getUsername())
                && !userRepository.existsByEmail(user.getEmail());
    }
}
