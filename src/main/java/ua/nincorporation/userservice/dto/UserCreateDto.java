package ua.nincorporation.userservice.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UserCreateDto(@NotEmpty(message = "Username should not be empty.")
                            @NotBlank(message = "Username should not be blank.")
                            @Size(min = 2, max = 64, message = "Username should be from 2 to 64 symbols.")
                            String username,
                            @NotEmpty(message = "Password should not be empty.")
                            @NotBlank(message = "Password should not be blank.")
                            @Size(min = 4, max = 32, message = "Password should be from 4 to 32 symbols.")
                            String password,
                            LocalDate dateOfBirth,
                            @Email
                            String email) {
}
