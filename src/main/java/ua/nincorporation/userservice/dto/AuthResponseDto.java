package ua.nincorporation.userservice.dto;

public record AuthResponseDto(String token, Long expiresIn) {
}
