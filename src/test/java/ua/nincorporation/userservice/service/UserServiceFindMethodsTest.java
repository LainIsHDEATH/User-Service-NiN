package ua.nincorporation.userservice.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.nincorporation.userservice.dto.UserReadDto;
import ua.nincorporation.userservice.mapper.UserReadMapper;
import ua.nincorporation.userservice.model.User;
import ua.nincorporation.userservice.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("User Service Find Methods")
class UserServiceFindMethodsTest {

    @Mock
    private UserRepository userRepository;
    @Mock private UserReadMapper userReadMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void findAll_returnsListOfDtos_whenRepositoryReturnsUsers() {
        User u1 = makeUser(1L, "alice", "alice@gmail.com");
        User u2 = makeUser(2L, "bob", "bob@gmail.com");

        UserReadDto d1 = makeUserReadDto("alice", "alice@gmail.com");
        UserReadDto d2 = makeUserReadDto("bob", "bob@gmail.com");

        doReturn(List.of(u1, u2)).when(userRepository).findAll();
        doReturn(d1).when(userReadMapper).toDto(u1);
        doReturn(d2).when(userReadMapper).toDto(u2);

        List<UserReadDto> result = userService.findAllUsers();

        assertThat(result).hasSize(2).containsExactly(d1, d2);
        verify(userRepository).findAll();
        verify(userReadMapper).toDto(u1);
        verify(userReadMapper).toDto(u2);
        verifyNoMoreInteractions(userRepository, userReadMapper);
    }

    @Test
    void findAll_returnsEmptyList_whenRepositoryReturnsEmpty() {
        doReturn(List.of()).when(userRepository).findAll();

        List<UserReadDto> result = userService.findAllUsers();

        assertThat(result).isEmpty();
        verify(userRepository).findAll();
        verifyNoInteractions(userReadMapper);
    }

    @Test
    void findAllUsers_propagatesRepositoryException() {
        doThrow(new RuntimeException("db down")).when(userRepository).findAll();
        assertThatThrownBy(() -> userService.findAllUsers())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("db down");
        verify(userRepository).findAll();
    }

    @Test
    void findById_returnsDto_whenUserFoundById() {
        User user = makeUser(1L, "ivan", "ivan@gmail.com");
        UserReadDto dto = makeUserReadDto("ivan", "ivan@gmail.com");

        doReturn(Optional.of(user)).when(userRepository).findById(1L);
        doReturn(dto).when(userReadMapper).toDto(user);

        Optional<UserReadDto> opt = userService.findUserById(1L);

        assertThat(opt).isPresent().contains(dto);
        verify(userRepository).findById(1L);
        verify(userReadMapper).toDto(user);
        verifyNoMoreInteractions(userRepository, userReadMapper);
    }

    @Test
    void findById_returnsEmptyOptional_whenUserNotFoundById() {
        doReturn(Optional.empty()).when(userRepository).findById(123L);

        Optional<UserReadDto> opt = userService.findUserById(123L);
        assertThat(opt).isEmpty();
        verify(userRepository).findById(123L);
        verifyNoInteractions(userReadMapper);
    }

    @Test
    void findByEmail_returnsDto_whenUserFoundByEmail() {
        User user = makeUser(1L, "maria", "maria@gmail.com");
        UserReadDto dto = makeUserReadDto("maria", "maria@gmail.com");

        doReturn(Optional.of(user)).when(userRepository).findByEmail("maria@gmail.com");
        doReturn(dto).when(userReadMapper).toDto(user);

        Optional<UserReadDto> opt = userService.findByEmail("maria@gmail.com");

        assertThat(opt).isPresent().contains(dto);
        verify(userRepository).findByEmail("maria@gmail.com");
        verify(userReadMapper).toDto(user);
        verifyNoMoreInteractions(userRepository, userReadMapper);
    }

    @Test
    void findByEmail_returnsEmpty_whenEmailNotFound() {
        doReturn(Optional.empty()).when(userRepository).findByEmail("no@one.com");

        Optional<UserReadDto> opt = userService.findByEmail("no@one.com");

        assertThat(opt).isEmpty();
        verify(userRepository).findByEmail("no@one.com");
        verifyNoInteractions(userReadMapper);
    }

    @Test
    void findByUsername_returnsDto_whenUserFoundByUsername() {
        User user = makeUser(1L, "sam", "sam@gmail.com");
        UserReadDto dto = makeUserReadDto("sam", "sam@gmail.com");

        doReturn(Optional.of(user)).when(userRepository).findByUsername("sam");
        doReturn(dto).when(userReadMapper).toDto(user);

        Optional<UserReadDto> opt = userService.findByUsername("sam");

        assertThat(opt).isPresent().contains(dto);
        verify(userRepository).findByUsername("sam");
        verify(userReadMapper).toDto(user);
        verifyNoMoreInteractions(userRepository, userReadMapper);
    }

    @Test
    void findByUsername_returnsEmpty_whenUsernameNotFound() {
        doReturn(Optional.empty()).when(userRepository).findByUsername("unknown");

        Optional<UserReadDto> opt = userService.findByUsername("unknown");

        assertThat(opt).isEmpty();
        verify(userRepository).findByUsername("unknown");
        verifyNoInteractions(userReadMapper);
    }

    private static User makeUser(Long id, String username, String email) {
        return User.builder()
                .id(id)
                .username(username)
                .email(email)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .password("pwd")
                .build();
    }

    private static UserReadDto makeUserReadDto(String username, String email) {
        return new UserReadDto(username, email, LocalDate.of(1990,1,1));
    }
}
