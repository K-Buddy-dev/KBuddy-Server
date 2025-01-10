package com.example.kbuddy_backend.auth.config;

import static jakarta.servlet.http.HttpServletResponse.*;

import com.example.kbuddy_backend.common.advice.response.ApiResponse;
import com.example.kbuddy_backend.common.advice.response.CustomCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        

        response.setStatus(SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        //필요한 권한 없이 접근시 403에러 반환
        response.getWriter().write(
                ApiResponse.error("권한이 없습니다.", request.getRequestURI(),403, CustomCode.HTTP_403.getCode()).toJson());
    }
}
