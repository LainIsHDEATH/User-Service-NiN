package ua.nincorporation.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.nincorporation.userservice.dto.UserCreateDto;
import ua.nincorporation.userservice.dto.UserReadDto;
import ua.nincorporation.userservice.dto.UserUpdateDTO;
import ua.nincorporation.userservice.service.UserService;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.ResponseEntity.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

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

    @PostMapping
    public ResponseEntity<UserReadDto> createUser(@RequestBody @Valid UserCreateDto userDto) {
        log.debug("Create user request for username = {}", userDto.username());

        UserReadDto created = userService.createUser(userDto);

        return status(CREATED).body(created);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserReadDto> updateUser(@PathVariable Long id,
                                                  @RequestBody UserUpdateDTO userDto) {
        log.debug("Patch update user request for id = {}", id);

        return userService.updateUser(id, userDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.debug("Delete user request for id = {}", id);

        return userService.deleteUser(id)
                ? noContent().build()
                : notFound().build();
    }
}
