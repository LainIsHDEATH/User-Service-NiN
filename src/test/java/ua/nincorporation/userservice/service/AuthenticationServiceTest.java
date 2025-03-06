package ua.nincorporation.userservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import ua.nincorporation.userservice.dto.UserCreateDto;
import ua.nincorporation.userservice.mapper.UserCreateMapper;
import ua.nincorporation.userservice.model.Role;
import ua.nincorporation.userservice.model.User;
import ua.nincorporation.userservice.repository.UserRepository;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserCreateMapper userCreateMapper;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void register_success_encodesPasswordSetsRoleAndSaves() {
        UserCreateDto dto = sampleDto();
        User user = mapperResult();

        doReturn(user).when(userCreateMapper).toEntity(dto);
        doReturn("encodedPassword").when(passwordEncoder).encode("rawPassword");

        User saved = User.builder()
                .id(1L)
                .username("Ivan")
                .password("encodedPassword")
                .email("ivan@gmail.com")
                .dateOfBirth(LocalDate.of(1990,1,1))
                .role(Role.USER)
                .build();

        doReturn(saved).when(userRepository).save(user);

        User result = authenticationService.register(dto);

        assertThat(result).isSameAs(saved);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User toSave = captor.getValue();

        assertThat(toSave.getPassword()).isEqualTo("encodedPassword");
        assertThat(toSave.getRole()).isEqualTo(Role.USER);

        verify(userCreateMapper).toEntity(dto);
        verify(passwordEncoder).encode("rawPassword");
        verify(userRepository).save(user);
        verifyNoMoreInteractions(userCreateMapper, passwordEncoder, userRepository);
    }

    @Test
    void register_propagatesDataIntegrityViolation_FromRepository() {
        UserCreateDto dto = sampleDto();
        User user = mapperResult();

        doReturn(user).when(userCreateMapper).toEntity(dto);
        doReturn("encodedPassword").when(passwordEncoder).encode("rawPassword");

        doThrow(new DataIntegrityViolationException("unique constraint")).when(userRepository).save(user);

        assertThatThrownBy(() -> authenticationService.register(dto))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("unique constraint");

        verify(userCreateMapper).toEntity(dto);
        verify(passwordEncoder).encode("rawPassword");
        verify(userRepository).save(user);
        verifyNoMoreInteractions(userCreateMapper, passwordEncoder, userRepository);
    }

    private static UserCreateDto sampleDto() {
        return new UserCreateDto(
                "Ivan",
                "rawPassword",
                LocalDate.of(1990,1,1),
                "ivan@gmail.com");
    }

    private static User mapperResult() {
        return User.builder()
                .username("Ivan")
                .password("rawPassword")
                .dateOfBirth(LocalDate.of(1990,1,1))
                .email("ivan@gmail.com")
                .build();
    }
}