package ua.nincorporation.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.nincorporation.userservice.dto.UserCreateDto;
import ua.nincorporation.userservice.dto.UserReadDto;
import ua.nincorporation.userservice.dto.UserUpdateDTO;
import ua.nincorporation.userservice.exception.ConflictException;
import ua.nincorporation.userservice.mapper.UserCreateMapper;
import ua.nincorporation.userservice.mapper.UserReadMapper;
import ua.nincorporation.userservice.mapper.UserUpdateMapper;
import ua.nincorporation.userservice.model.User;
import ua.nincorporation.userservice.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final UserReadMapper userReadMapper;
    private final UserCreateMapper userCreateMapper;
    private final UserUpdateMapper userUpdateMapper;

    public List<UserReadDto> findAllUsers() {
        return userRepository.findAll().stream()
                .map(userReadMapper::toDto)
                .toList();
    }

    public Optional<UserReadDto> findUserById(Long id) {
        return userRepository.findById(id)
                .map(userReadMapper::toDto);
    }

    public Optional<UserReadDto> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userReadMapper::toDto);
    }

    public Optional<UserReadDto> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userReadMapper::toDto);
    }

    @Transactional
    public UserReadDto createUser(UserCreateDto userDto) {
        return Optional.ofNullable(userDto)
                .map(userCreateMapper::toEntity)
                .map(user -> {
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                    return user;
                })
                .map(userRepository::save)
                .map(userReadMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("userDto is null"));
    }

    @Transactional
    public Optional<UserReadDto> updateUser(Long id, UserUpdateDTO updatedUser) {
        return userRepository.findById(id)
                .map(user -> {
                    String newUsername = updatedUser.username();
                    if (newUsername != null && !newUsername.isBlank() && !newUsername.equals(user.getUsername())) {
                        if (userRepository.existsByUsernameAndIdNot(newUsername.trim(), id)) {
                            throw new ConflictException("Username already in use");
                        }
                    }

                    String newEmail = updatedUser.email();
                    if (newEmail != null && !newEmail.isBlank() && !newEmail.equalsIgnoreCase(user.getEmail())) {
                        if (userRepository.existsByEmailAndIdNot(newEmail.trim().toLowerCase(), id)) {
                            throw new ConflictException("Email already in use");
                        }
                    }

                    User userUpdated = userUpdateMapper.update(user, updatedUser);
                    return userRepository.saveAndFlush(userUpdated);
                })
                .map(userReadMapper::toDto);
    }

    @Transactional
    public boolean deleteUser(Long id) {
        return userRepository.findById(id)
                .map(entity -> {
                    userRepository.delete(entity);
                    userRepository.flush();
                    return true;
                })
                .orElse(false);
    }
}
