package com.example.kbuddy_backend.admin.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.kbuddy_backend.admin.config.AdminCredentialsProperties;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class AdminBasicAuthFilterTest {

    private AdminCredentialsProperties properties;
    private AdminBasicAuthFilter filter;

    @BeforeEach
    void setUp() {
        properties = new AdminCredentialsProperties();
        properties.setId("admin");
        properties.setPassword("password");
        filter = new AdminBasicAuthFilter(properties);
        SecurityContextHolder.clearContext();
    }

    @DisplayName("관리자 경로가 아니면 필터가 동작하지 않는다")
    @Test
    void shouldSkipNonAdminPath() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/kbuddy/v1/blog");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @DisplayName("Basic Auth 헤더가 없으면 401을 반환한다")
    @Test
    void shouldRejectMissingHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/subscribers/stats");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @DisplayName("정상 자격 증명으로 요청하면 인증이 설정된다")
    @Test
    void shouldAuthenticateWithValidCredentials() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/subscribers/stats");
        String encoded = Base64.getEncoder()
                .encodeToString("admin:password".getBytes(StandardCharsets.UTF_8));
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority")
                .contains("ROLE_ADMIN");
    }
}


