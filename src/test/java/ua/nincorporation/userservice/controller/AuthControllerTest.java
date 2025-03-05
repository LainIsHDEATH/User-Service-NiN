package ua.nincorporation.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import ua.nincorporation.userservice.dto.AuthRequestDTO;
import ua.nincorporation.userservice.dto.UserCreateDto;
import ua.nincorporation.userservice.model.Role;
import ua.nincorporation.userservice.model.User;
import ua.nincorporation.userservice.security.CustomUserDetails;
import ua.nincorporation.userservice.security.JWTUtil;
import ua.nincorporation.userservice.service.AuthenticationService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@RequiredArgsConstructor
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AuthenticationService authenticationService;
    @MockitoBean
    JWTUtil jwtUtil;
    @MockitoBean
    AuthenticationManager authenticationManager;

    @Test
    void register_success_returnsCreatedAndHeader() throws Exception {
        UserCreateDto createDto = new UserCreateDto(
                "Ivan",
                "rawPassword",
                LocalDate.of(1990,1,1),
                "ivan@gmail.com");
        User savedUser = User.builder()
                .id(1L)
                .username("Ivan")
                .password("encodedPassword")
                .email("ivan@gmail.com")
                .dateOfBirth(LocalDate.of(1990,1,1))
                .role(Role.USER)
                .build();

        doReturn(savedUser).when(authenticationService).register(createDto);
        doReturn("jwt-token-xyz").when(jwtUtil).generateToken(eq(1L), eq("Ivan"), anyList());
        doReturn(60L).when(jwtUtil).getExpirationSeconds();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, "Bearer jwt-token-xyz"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("jwt-token-xyz"))
                .andExpect(jsonPath("$.expiresIn").value(60));

        verify(authenticationService).register(createDto);
        verify(jwtUtil).generateToken(eq(1L), eq("Ivan"), anyList());
        verify(jwtUtil).getExpirationSeconds();
    }

    @Test
    void authenticate_success_returnsOk_andHeader() throws Exception {
        AuthRequestDTO req = new AuthRequestDTO("Ivan","rawPassword");

        User user = User.builder().
                id(1L)
                .username("Ivan")
                .password("encodedPassword")
                .role(Role.USER)
                .build();
        CustomUserDetails principal = new CustomUserDetails(user);

        Authentication auth = mock(Authentication.class);
        doReturn(principal).when(auth).getPrincipal();
        doReturn(auth).when(authenticationManager).authenticate(any());

        doReturn("jwt-token-xyz").when(jwtUtil).generateToken(eq(1L), eq("Ivan"), anyList());
        doReturn(60L).when(jwtUtil).getExpirationSeconds();

        mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, "Bearer jwt-token-xyz"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("jwt-token-xyz"))
                .andExpect(jsonPath("$.expiresIn").value(60L));

        verify(authenticationManager).authenticate(any());
        verify(jwtUtil).generateToken(1L, "Ivan", List.of("USER"));
        verify(jwtUtil).getExpirationSeconds();
    }

    @Test
    void authenticate_badCredentials_shouldBeMappedTo401_if_controllerAdviceConfigured() throws Exception {
        AuthRequestDTO req = new AuthRequestDTO("Ivan","wrong");
        doThrow(new BadCredentialsException("Bad credentials")).when(authenticationManager).authenticate(any());

        mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());

        verify(authenticationManager).authenticate(any());
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void register_validationError_returns400_whenRequiredMissing() throws Exception {
        UserCreateDto invalid = new UserCreateDto("", "", null, "not-an-email");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authenticationService, jwtUtil);
    }
}