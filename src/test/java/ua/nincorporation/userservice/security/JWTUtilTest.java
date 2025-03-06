package ua.nincorporation.userservice.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JWTUtilTest {

    @Test
    void generateAndParseToken() {
        String secret = "0123456789abcdef0123456789abcdef";
        JWTUtil jwtUtil = new JWTUtil(secret, false, "NiN", 60L, 60L);

        String token = jwtUtil.generateToken(7L, "ivan", List.of("USER","ADMIN"));

        assertThat(jwtUtil.tryExtractUserId(token)).contains(7L);
        assertThat(jwtUtil.tryExtractUsername(token)).contains("ivan");
        assertThat(jwtUtil.tryExtractRoles(token)).containsExactlyInAnyOrder("USER","ADMIN");

        UserDetails ud = org.springframework.security.core.userdetails.User
                .withUsername("ivan").password("x").authorities("USER", "ADMIN").build();
        assertThat(jwtUtil.validateToken(token, ud)).isTrue();
    }

}