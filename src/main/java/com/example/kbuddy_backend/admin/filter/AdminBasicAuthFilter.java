package com.example.kbuddy_backend.admin.filter;

import com.example.kbuddy_backend.admin.config.AdminCredentialsProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class AdminBasicAuthFilter extends OncePerRequestFilter {

    private static final String ADMIN_REALM = "KBuddy Admin";
    private final AdminCredentialsProperties credentialsProperties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        final String uri = request.getRequestURI();
        return uri == null || !uri.startsWith("/admin")
                || "/admin/login".equals(uri)
                || "/admin/refresh".equals(uri)
                || "/kbuddy/v1/admin/login".equals(uri);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!credentialsProperties.isConfigured()) {
            reject(response);
            return;
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (header == null || !header.startsWith("Basic ")) {
            reject(response);
            return;
        }

        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(header.substring(6).trim()), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            reject(response);
            return;
        }

        int separatorIndex = decoded.indexOf(':');
        if (separatorIndex < 0) {
            reject(response);
            return;
        }

        String inputId = decoded.substring(0, separatorIndex);
        String inputPassword = decoded.substring(separatorIndex + 1);

        if (!credentialsProperties.matches(inputId, inputPassword)) {
            reject(response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "ADMIN",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContextHolderStrategy().getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"" + ADMIN_REALM + "\"");
        response.getWriter().write("Unauthorized");
    }
}
