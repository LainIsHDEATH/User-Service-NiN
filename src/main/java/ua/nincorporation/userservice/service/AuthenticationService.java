package ua.nincorporation.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.nincorporation.userservice.dto.UserCreateDto;
import ua.nincorporation.userservice.mapper.UserCreateMapper;
import ua.nincorporation.userservice.model.Role;
import ua.nincorporation.userservice.model.User;
import ua.nincorporation.userservice.repository.UserRepository;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final UserCreateMapper userCreateMapper;

    @Transactional
    public User register(UserCreateDto userDto){
        User user = preparePersonForRegistration(userDto);

        return userRepository.save(user);
    }

    private User preparePersonForRegistration(UserCreateDto userDto) {
        User user = userCreateMapper.toEntity(userDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        return user;
    }
}
