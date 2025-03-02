package ua.nincorporation.userservice.mapper;

import org.mapstruct.Mapper;
import ua.nincorporation.userservice.dto.UserCreateDto;
import ua.nincorporation.userservice.model.User;

@Mapper(componentModel = "spring")
public interface UserCreateMapper {
    UserCreateDto toDto(User user);

    User toEntity(UserCreateDto userCreateDto);
}
