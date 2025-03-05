package ua.nincorporation.userservice.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ua.nincorporation.userservice.dto.UserCreateDto;
import ua.nincorporation.userservice.dto.UserReadDto;
import ua.nincorporation.userservice.model.User;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {UserCreateMapperImpl.class})
class UserCreateMapperTest {

    @Autowired
    private UserCreateMapper mapper;

    @Test
    void toDto_and_back_should_map_fields() {
        User user = User.builder()
                .id(1L)
                .username("Ivan")
                .password("1234")
                .email("ivan@example.com")
                .dateOfBirth(LocalDate.of(1990,1,1))
                .build();

        UserCreateDto dto = mapper.toDto(user);

        assertThat(dto).isNotNull();
        assertThat(dto.username()).isEqualTo("Ivan");
        assertThat(dto.password()).isEqualTo("1234");
        assertThat(dto.email()).isEqualTo("ivan@example.com");
        assertThat(dto.dateOfBirth()).isEqualTo(LocalDate.of(1990,1,1));

        User fromDto = mapper.toEntity(dto);
        assertThat(fromDto.getUsername()).isEqualTo("Ivan");
        assertThat(fromDto.getPassword()).isEqualTo("1234");
        assertThat(fromDto.getEmail()).isEqualTo("ivan@example.com");
        assertThat(fromDto.getDateOfBirth()).isEqualTo(LocalDate.of(1990,1,1));
    }

    @Test
    void toDto_handlesNullInput() {
        assertThat(mapper.toDto(null)).isNull();
    }
}