package ua.nincorporation.userservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ua.nincorporation.userservice.dto.UserCreateDto;
import ua.nincorporation.userservice.dto.UserReadDto;
import ua.nincorporation.userservice.dto.UserUpdateDTO;
import ua.nincorporation.userservice.exception.ConflictException;
import ua.nincorporation.userservice.mapper.UserCreateMapper;
import ua.nincorporation.userservice.mapper.UserReadMapper;
import ua.nincorporation.userservice.mapper.UserUpdateMapper;
import ua.nincorporation.userservice.model.User;
import ua.nincorporation.userservice.repository.UserRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTransactionTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserReadMapper userReadMapper;
    @Mock
    private UserCreateMapper userCreateMapper;
    @Mock
    private UserUpdateMapper userUpdateMapper;
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
    void updateUser_success() {
        User userFromDb = User.builder()
                .id(1L)
                .username("Ivan")
                .password("encoded1234")
                .email("ivan@gmail.com")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();
        UserUpdateDTO updateDto = new UserUpdateDTO(
                "Bob",
                "5678",
                LocalDate.of(1991, 2, 2),
                "bob@gmail.com");
        UserReadDto userReadDto = new UserReadDto(
                "Bob",
                "bob@gmail.com",
                LocalDate.of(1991, 2, 2));

        doReturn(Optional.of(userFromDb)).when(userRepository).findById(1L);
        doReturn(false).when(userRepository).existsByUsernameAndIdNot("Bob".trim(), 1L);
        doReturn(false).when(userRepository).existsByEmailAndIdNot("bob@gmail.com".trim().toLowerCase(), 1L);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            UserUpdateDTO update = invocation.getArgument(1);
            user.setUsername(update.username());
            user.setPassword("encoded5678");
            user.setEmail(update.email());
            user.setDateOfBirth(update.dateOfBirth());
            return user;
        }).when(userUpdateMapper).update(userFromDb, updateDto);
        doReturn(userFromDb).when(userRepository).saveAndFlush(userFromDb);
        doReturn(userReadDto).when(userReadMapper).toDto(userFromDb);

        Optional<UserReadDto> result = userService.updateUser(1L, updateDto);

        assertThat(result).isPresent().contains(userReadDto);
        verify(userRepository).findById(1L);
        verify(userRepository).existsByUsernameAndIdNot("Bob".trim(), 1L);
        verify(userRepository).existsByEmailAndIdNot("bob@gmail.com".trim().toLowerCase(), 1L);
        verify(userUpdateMapper).update(userFromDb, updateDto);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(captor.capture());
        User savedArg = captor.getValue();
        assertThat(savedArg.getUsername()).isEqualTo("Bob");
        assertThat(savedArg.getEmail()).isEqualTo("bob@gmail.com");
        verify(userReadMapper).toDto(userFromDb);
        verifyNoMoreInteractions(userRepository, passwordEncoder, userReadMapper, userCreateMapper, userUpdateMapper);
    }

    @Test
    void updateUser_returnsEmpty_whenUserNotFound() {
        UserUpdateDTO updateDto = new UserUpdateDTO(
                "Bob",
                "5678",
                LocalDate.of(1991, 2, 2),
                "bob@gmail.com");

        doReturn(Optional.empty()).when(userRepository).findById(123L);

        Optional<UserReadDto> result = userService.updateUser(123L, updateDto);

        assertThat(result).isEmpty();
        verify(userRepository).findById(123L);
        verifyNoMoreInteractions(userRepository, passwordEncoder, userReadMapper, userCreateMapper, userUpdateMapper);
    }

    @Test
    void updateUser_throwsException_whenUsernameAlreadyExists() {
        User userFromDb = User.builder()
                .id(1L)
                .username("Ivan")
                .password("encoded1234")
                .email("ivan@gmail.com")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();
        UserUpdateDTO updateDto = new UserUpdateDTO(
                "Bob",
                "5678",
                LocalDate.of(1991, 2, 2),
                "bob@gmail.com");

        doReturn(Optional.of(userFromDb)).when(userRepository).findById(1L);
        doReturn(true).when(userRepository).existsByUsernameAndIdNot("Bob".trim(), 1L);

        assertThatThrownBy(() -> userService.updateUser(1L, updateDto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Username already in use");
        verify(userRepository).findById(1L);
        verify(userRepository).existsByUsernameAndIdNot("Bob".trim(), 1L);
        verifyNoMoreInteractions(userRepository, passwordEncoder, userReadMapper, userCreateMapper, userUpdateMapper);
    }

    @Test
    void updateUser_throwsException_whenEmailAlreadyExists() {
        User userFromDb = User.builder()
                .id(1L)
                .username("Ivan")
                .password("encoded1234")
                .email("ivan@gmail.com")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();
        UserUpdateDTO updateDto = new UserUpdateDTO(
                "Bob",
                "5678",
                LocalDate.of(1991, 2, 2),
                "bob@gmail.com");


        doReturn(Optional.of(userFromDb)).when(userRepository).findById(1L);
        doReturn(false).when(userRepository).existsByUsernameAndIdNot("Bob".trim(), 1L);
        doReturn(true).when(userRepository).existsByEmailAndIdNot("bob@gmail.com".trim().toLowerCase(), 1L);

        assertThatThrownBy(() -> userService.updateUser(1L, updateDto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already in use");
        verify(userRepository).findById(1L);
        verify(userRepository).existsByUsernameAndIdNot("Bob".trim(), 1L);
        verify(userRepository).existsByEmailAndIdNot("bob@gmail.com".trim().toLowerCase(), 1L);
        verifyNoMoreInteractions(userRepository, passwordEncoder, userReadMapper, userCreateMapper, userUpdateMapper);
    }

    @Test
    void deleteUser_returnsTrue_whenUserFoundById() {
        User userFromDb = getUserSaved();

        doReturn(Optional.of(userFromDb)).when(userRepository).findById(1L);

        boolean result = userService.deleteUser(1L);

        assertThat(result).isTrue();
        verify(userRepository).findById(1L);
        verify(userRepository).delete(userFromDb);
        verify(userRepository).flush();
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void deleteUser_returnsFalse_whenUserNotFoundById() {
        doReturn(Optional.empty()).when(userRepository).findById(123L);

        boolean result = userService.deleteUser(123L);

        assertThat(result).isFalse();
        verify(userRepository).findById(123L);
        verifyNoMoreInteractions(userRepository);
    }

    private static UserCreateDto getUserCreateDto() {
        return new UserCreateDto(
                "Ivan",
                "1234",
                LocalDate.of(1990, 1, 1),
                "ivan@gmail.com");
    }

    private static User getUser() {
        return User.builder()
                .username("Ivan")
                .password("1234")
                .email("ivan@gmail.com")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();
    }

    private static User getUserSaved() {
        return User.builder()
                .id(1L)
                .username("Ivan")
                .password("encoded1234")
                .email("ivan@gmail.com")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();
    }

    private static UserReadDto getUserReadDto() {
        return new UserReadDto(
                "Ivan",
                "ivan@gmail.com",
                LocalDate.of(1990, 1, 1));
    }
}