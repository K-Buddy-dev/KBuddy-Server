package com.example.kbuddy_backend.admin.config;

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
}


