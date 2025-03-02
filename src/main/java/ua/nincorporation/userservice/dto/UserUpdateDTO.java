package ua.nincorporation.userservice.dto;

import java.time.LocalDate;

public record UserUpdateDTO(String username,
                            String password,
                            LocalDate dateOfBirth,
                            String email) {
}
