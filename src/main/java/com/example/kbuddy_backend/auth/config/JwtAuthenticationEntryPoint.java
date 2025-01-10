package com.example.kbuddy_backend.auth.config;

import com.example.kbuddy_backend.common.advice.response.ApiResponse;
import com.example.kbuddy_backend.common.advice.response.CustomCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        //유효한 자격증명이 없을 시 401에러 반환
        response.getWriter().write(ApiResponse.error("유효한 자격증명이 없습니다.", request.getRequestURI(),401,CustomCode.HTTP_401.getCode()).toJson());
    }
}
