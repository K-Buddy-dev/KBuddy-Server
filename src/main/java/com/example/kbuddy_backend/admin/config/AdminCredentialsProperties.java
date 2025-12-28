package com.example.kbuddy_backend.admin.config;

import java.nio.charset.StandardCharsets;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "admin.credentials")
public class AdminCredentialsProperties {

    private String id;
    private String password;

    public boolean isConfigured() {
        return StringUtils.hasText(id) && StringUtils.hasText(password);
    }

    public boolean matches(String inputId, String inputPassword) {
        if (!isConfigured()) {
            return false;
        }
        return constantTimeEquals(id, inputId) && constantTimeEquals(password, inputPassword);
    }

    private boolean constantTimeEquals(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        byte[] actualBytes = actual.getBytes(StandardCharsets.UTF_8);
        if (expectedBytes.length != actualBytes.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < expectedBytes.length; i++) {
            result |= expectedBytes[i] ^ actualBytes[i];
        }
        return result == 0;
    }
}

