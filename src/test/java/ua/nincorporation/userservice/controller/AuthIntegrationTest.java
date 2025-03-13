package ua.nincorporation.userservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;
import ua.nincorporation.userservice.dto.AuthRequestDTO;
import ua.nincorporation.userservice.dto.UserCreateDto;
import ua.nincorporation.userservice.integration.IntegrationTestBase;
import ua.nincorporation.userservice.model.User;
import ua.nincorporation.userservice.repository.UserRepository;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthIntegrationTest extends IntegrationTestBase {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;

    @BeforeEach
    void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    void register_then_authenticate_then_accessProtectedEndpoint() throws Exception {
        // 1) register
        UserCreateDto dto = new UserCreateDto("intuser", "pass1234", LocalDate.of(1990,1,1), "int@example.com");
        MvcResult registerResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        // Optionally verify user created in DB
        User created = userRepository.findByUsername("intuser").orElseThrow();
        assertThat(created.getEmail()).isEqualTo("int@example.com");

        // 2) authenticate
        AuthRequestDTO authReq = new AuthRequestDTO("intuser", "pass1234");
        MvcResult authResult = mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authReq)))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.AUTHORIZATION))
                .andReturn();

        String authHeader = authResult.getResponse().getHeader(HttpHeaders.AUTHORIZATION);
        assertThat(authHeader).isNotBlank().startsWith("Bearer ");

        String token = authHeader.substring(7);

        // 3) access protected resource (GET /users requires authentication)
        mockMvc.perform(get("/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
