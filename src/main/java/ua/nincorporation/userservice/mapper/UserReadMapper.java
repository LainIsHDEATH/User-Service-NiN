package ua.nincorporation.userservice.mapper;

import org.mapstruct.Mapper;
import ua.nincorporation.userservice.dto.UserReadDto;
import ua.nincorporation.userservice.model.User;

@Mapper(componentModel = "spring")
public interface UserReadMapper {
    UserReadDto toDto(User user);

    User toEntity(UserReadDto userReadDto);
}
