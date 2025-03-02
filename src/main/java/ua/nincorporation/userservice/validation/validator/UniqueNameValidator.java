package ua.nincorporation.userservice.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ua.nincorporation.userservice.repository.UserRepository;
import ua.nincorporation.userservice.validation.annotation.UniqueEmail;

@Component
@RequiredArgsConstructor
public class UniqueNameValidator implements ConstraintValidator<UniqueEmail, String> {
    private final UserRepository userRepository;

    @Override
    public boolean isValid(String name, ConstraintValidatorContext ctx) {
        if (name == null || name.isBlank()) return true;
        return !userRepository.existsByEmail(name);
    }
}
