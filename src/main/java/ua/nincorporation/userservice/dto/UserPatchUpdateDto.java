package ua.nincorporation.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;
import java.util.Optional;

public record UserPatchUpdateDto(
        Optional<@NotBlank(message = "Username must not be blank.") String> username,
        Optional<@NotBlank(message = "Password must not be blank.") String> password,
        Optional<@Email(message = "Invalid email.") String> email,
        Optional<@Past(message = "Date of birth must be in the past.") LocalDate> dateOfBirth
) {}
