package ua.nincorporation.userservice.dto;

import java.time.LocalDate;

public record UserReadDto(Long id,
                          String username,
                          String email,
                          LocalDate dateOfBirth) {
}
