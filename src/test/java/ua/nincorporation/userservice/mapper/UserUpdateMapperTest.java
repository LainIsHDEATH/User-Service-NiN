package ua.nincorporation.userservice.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ua.nincorporation.userservice.dto.UserUpdateDTO;
import ua.nincorporation.userservice.model.User;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class UserUpdateMapperTest {

    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserUpdateMapper mapper;

    @Test
    void update_mapsFields() {
        User user = User.builder()
                .id(1L)
                .username("Ivan")
                .password("encoded1234")
                .email("ivan@gmail.com")
                .dateOfBirth(LocalDate.of(1990,1,1))
                .build();
        UserUpdateDTO updateDto = new UserUpdateDTO(
                "Bob",
                "5678",
                LocalDate.of(1991, 2, 2),
                "bob@gmail.com");

        doReturn("encoded5678").when(passwordEncoder).encode("5678");

        User updated = mapper.update(user, updateDto);

        assertThat(updated).isNotNull();
        assertThat(updated.getUsername()).isEqualTo("Bob");
        assertThat(updated.getPassword()).isEqualTo("encoded5678");
        assertThat(updated.getEmail()).isEqualTo("bob@gmail.com");
        assertThat(updated.getDateOfBirth()).isEqualTo(LocalDate.of(1991,2,2));
    }

    @Test
    void update_handlesNullInput() {
        assertThat(mapper.update(null, null)).isNull();
    }

    @Test
    void update_handlesNullUpdateInput() {
        User user = User.builder()
                .id(1L)
                .username("Ivan")
                .password("encoded1234")
                .email("ivan@gmail.com")
                .dateOfBirth(LocalDate.of(1990,1,1))
                .build();

        assertThat(mapper.update(user, null)).isEqualTo(user);
    }
}