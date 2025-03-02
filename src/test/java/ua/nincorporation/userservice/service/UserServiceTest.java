package ua.nincorporation.userservice.service;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ua.nincorporation.userservice.dto.UserCreateDto;
import ua.nincorporation.userservice.dto.UserReadDto;
import ua.nincorporation.userservice.mapper.UserCreateMapper;
import ua.nincorporation.userservice.mapper.UserReadMapper;
import ua.nincorporation.userservice.model.User;
import ua.nincorporation.userservice.repository.UserRepository;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserReadMapper userReadMapper;
    @Mock
    private UserCreateMapper userCreateMapper;
    @InjectMocks
    private UserService userService;

    @Test
    void createUser_success() {
        UserCreateDto userCreateDto = getUserCreateDto();
        User user = getUser();

        doReturn(user).when(userCreateMapper).toEntity(userCreateDto);
        doReturn("encoded1234").when(passwordEncoder).encode(userCreateDto.password());

        User saved = getUserSaved();
        UserReadDto userReadDto = getUserReadDto();

        doReturn(saved).when(userRepository).save(user);
        doReturn(userReadDto).when(userReadMapper).toDto(saved);

        UserReadDto result = userService.createUser(userCreateDto);

        assertThat(result.username()).isEqualTo("Ivan");
        assertThat(result.email()).isEqualTo("ivan@gmail.com");
        verify(userCreateMapper).toEntity(userCreateDto);
        verify(passwordEncoder).encode("1234");
        verify(userRepository).save(argThat(u -> "encoded1234".equals(u.getPassword()) && "Ivan".equals(u.getUsername())));
        verify(userReadMapper).toDto(saved);
        verifyNoMoreInteractions(userRepository, passwordEncoder, userReadMapper, userCreateMapper);
    }

    @Test
    void updateUser() {
    }

    @Test
    void deleteUser() {
    }

    private static UserCreateDto getUserCreateDto() {
        return new UserCreateDto(
                "Ivan",
                "1234",
                LocalDate.of(1990, 1, 1),
                "ivan@gmail.com");
    }

    private static User getUser() {
        User user = User.builder()
                .username("Ivan")
                .password("1234")
                .email("ivan@gmail.com")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();
        return user;
    }

    private static User getUserSaved() {
        User saved = User.builder()
                .id(1L)
                .username("Ivan")
                .password("encoded1234")
                .email("ivan@gmail.com")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();
        return saved;
    }

    private static UserReadDto getUserReadDto() {
        return new UserReadDto(
                "Ivan",
                "ivan@gmail.com",
                LocalDate.of(1990, 1, 1));
    }
}