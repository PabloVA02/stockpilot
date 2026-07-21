package com.pabloverdejo.stockpilot.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityConfigTest {

    private final SecurityConfig config = new SecurityConfig();
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void rejectsSigningSecretsShorterThanHs256Requires() {
        assertThatThrownBy(() -> config.jwtSigningKey("too-short"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }

    @Test
    void rejectsWeakLocalAccountPasswords() {
        assertThatThrownBy(() -> config.userDetailsService(
                encoder, "manager", "short", "viewer", "viewer-password"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("STOCKPILOT_MANAGER_PASSWORD");

        assertThatThrownBy(() -> config.userDetailsService(
                encoder, "manager", "manager-password", "viewer", "short"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("STOCKPILOT_VIEWER_PASSWORD");
    }
}
