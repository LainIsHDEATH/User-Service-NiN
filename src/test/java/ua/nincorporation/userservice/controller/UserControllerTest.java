package ua.nincorporation.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import ua.nincorporation.userservice.dto.UserCreateDto;
import ua.nincorporation.userservice.dto.UserReadDto;
import ua.nincorporation.userservice.dto.UserUpdateDTO;
import ua.nincorporation.userservice.service.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@RequiredArgsConstructor
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    // ---------------- GET /users ----------------
    @Test
    void getAllUsers_returnsList() throws Exception {
        UserReadDto userA = new UserReadDto("Alice", "alice@gmail.com", LocalDate.of(1990, 1, 1));
        UserReadDto userB = new UserReadDto("Bob", "bob@gmail.com", LocalDate.of(1992, 2, 2));

        doReturn(List.of(userA, userB)).when(userService).findAllUsers();

        mockMvc.perform(get("/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].username").value("Alice"))
                .andExpect(jsonPath("$[1].username").value("Bob"));

        verify(userService).findAllUsers();
        verifyNoMoreInteractions(userService);
    }

    // ---------------- GET /users/{id} ----------------
    @Test
    void getUserById_found_returns200() throws Exception {
        UserReadDto readDto = new UserReadDto("ivan", "ivan@gmail.com", LocalDate.of(1990,1,1));

        doReturn(Optional.of(readDto)).when(userService).findUserById(1L);

        mockMvc.perform(get("/users/{id}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ivan"))
                .andExpect(jsonPath("$.email").value("ivan@gmail.com"));

        verify(userService).findUserById(1L);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void getUserById_notFound_returns404() throws Exception {
        doReturn(Optional.empty()).when(userService).findUserById(123L);

        mockMvc.perform(get("/users/{id}", 123L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userService).findUserById(123L);
        verifyNoMoreInteractions(userService);
    }

    // ---------------- POST /users ----------------
    @Test
    void createUser_success_returns201_andBody() throws Exception {
        UserCreateDto createDto = new UserCreateDto("Ivan", "rawPassword", LocalDate.of(1990,1,1), "ivan@gmail.com");
        UserReadDto created = new UserReadDto("Ivan", "ivan@gmail.com", LocalDate.of(1990,1,1));

        doReturn(created).when(userService).createUser(createDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("Ivan"))
                .andExpect(jsonPath("$.email").value("ivan@gmail.com"));

        verify(userService).createUser(createDto);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void createUser_validationError_returns400() throws Exception {
        UserCreateDto invalid = new UserCreateDto("", "", null, "not-an-email");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    // ---------------- PATCH /users/{id} ----------------
    @Test
    void updateUser_success_returns200() throws Exception {
        UserUpdateDTO updateDto = new UserUpdateDTO("Bob","5678", LocalDate.of(1991,2,2), "bob@gmail.com");
        UserReadDto updated = new UserReadDto("Bob","bob@gmail.com", LocalDate.of(1991,2,2));

        doReturn(Optional.of(updated)).when(userService).updateUser(1L, updateDto);

        mockMvc.perform(patch("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Bob"))
                .andExpect(jsonPath("$.email").value("bob@gmail.com"));

        verify(userService).updateUser(eq(1L), any(UserUpdateDTO.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    void updateUser_notFound_returns404() throws Exception {
        UserUpdateDTO updateDto = new UserUpdateDTO("X", null, null, null);

        doReturn(Optional.empty()).when(userService).updateUser(123L, updateDto);

        mockMvc.perform(patch("/users/{id}", 123L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());

        verify(userService).updateUser(eq(123L), any(UserUpdateDTO.class));
        verifyNoMoreInteractions(userService);
    }

    // ---------------- DELETE /users/{id} ----------------
    @Test
    void deleteUser_whenFound_returns204() throws Exception {
        doReturn(true).when(userService).deleteUser(1L);

        mockMvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void deleteUser_whenNotFound_returns404() throws Exception {
        doReturn(false).when(userService).deleteUser(123L);

        mockMvc.perform(delete("/users/{id}", 123L))
                .andExpect(status().isNotFound());

        verify(userService).deleteUser(123L);
    }
}