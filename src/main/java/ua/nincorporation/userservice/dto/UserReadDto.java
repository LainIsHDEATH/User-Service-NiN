package ua.nincorporation.userservice.dto;

import java.time.LocalDate;

public record UserReadDto (String username,
                           String email,
                           LocalDate dateOfBirth) {
}
