package ua.nincorporation.userservice.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import ua.nincorporation.userservice.integration.IntegrationTestBase;
import ua.nincorporation.userservice.model.User;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback
class UserRepositoryIT extends IntegrationTestBase {

    @Autowired
    private UserRepository userRepository;

    private static User createUser(String username, String email) {
        return User.builder()
                .username(username)
                .password("pwd")
                .email(email)
                .dateOfBirth(LocalDate.of(1990,1,1))
                .build();
    }

    @Test
    void save_and_findByUsername() {
        var u = createUser("tcuser", "tc@example.com");
        userRepository.saveAndFlush(u);

        Optional<User> opt = userRepository.findByUsername("tcuser");
        assertThat(opt).isPresent();
        assertThat(opt.get().getEmail()).isEqualTo("tc@example.com");
    }

    @Test
    void existsByUsernameAndIdNot_and_existsByEmailAndIdNot() {
        User a = createUser("u1", "u1@example.com");
        User b = createUser("u2", "u2@example.com");
        userRepository.saveAndFlush(a);
        userRepository.saveAndFlush(b);

        assertThat(userRepository.existsByUsernameAndIdNot("u1", b.getId())).isTrue();
        assertThat(userRepository.existsByUsernameAndIdNot("u1", a.getId())).isFalse();

        assertThat(userRepository.existsByEmailAndIdNot("u2@example.com", a.getId())).isTrue();
        assertThat(userRepository.existsByEmailAndIdNot("u2@example.com", b.getId())).isFalse();
    }

    @Test
    void uniqueConstraint_onEmail_throwsException() {
        // предполагая, что в БД есть unique constraint на email (entity @Column(unique=true))
        User a = createUser("x", "same@example.com");
        User b = createUser("y", "same@example.com");
        userRepository.saveAndFlush(a);

        // при попытке сохранить b ожидаем DataIntegrityViolationException
        assertThatThrownBy(() -> {
            userRepository.saveAndFlush(b);
        }).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }
}