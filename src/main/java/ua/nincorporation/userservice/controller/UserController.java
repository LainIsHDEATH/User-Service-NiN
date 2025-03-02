package ua.nincorporation.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ua.nincorporation.userservice.dto.UserCreateDto;
import ua.nincorporation.userservice.dto.UserReadDto;
import ua.nincorporation.userservice.dto.UserUpdateDTO;
import ua.nincorporation.userservice.service.UserService;
import ua.nincorporation.userservice.util.UserValidator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.ResponseEntity.*;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.status;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserValidator userValidator;

    @GetMapping
    public ResponseEntity<List<UserReadDto>> getAllUsers() {
        List<UserReadDto> users = userService.findAllUsers();
        return ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserReadDto> getUserById(@PathVariable Long id) {
        return userService.findUserById(id)
                .map(ResponseEntity::ok)
                .orElse(notFound().build());
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserReadDto> createUser(@RequestBody @Valid UserCreateDto userDto) {
        log.debug("Create user request for username = {}", userDto.username());

        UserReadDto created = userService.createUser(userDto);

        return status(HttpStatus.CREATED).body(userService.createUser(userDto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserReadDto> updateUser(@PathVariable Long id,
                                                  @RequestBody UserUpdateDTO userDto) {
        log.debug("Patch update user request for username = {}", userDto.username());

        return userService.updateUser(id, userDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id)
                ? noContent().build()
                : notFound().build();
    }
}
