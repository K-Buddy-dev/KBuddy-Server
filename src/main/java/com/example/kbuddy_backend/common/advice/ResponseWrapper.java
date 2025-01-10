package com.example.kbuddy_backend.common.advice;

import com.example.kbuddy_backend.common.advice.response.ApiResponse;
import com.example.kbuddy_backend.common.advice.response.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(basePackages = "com.example.kbuddy_backend")
public class ResponseWrapper implements ResponseBodyAdvice<Object> {

    //ConvertType과 ReturnType을 통해 해당 어드바이스를 적용할지 결정.
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Nullable
    @Override
    public Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        HttpServletResponse servletResponse = ((ServletServerHttpResponse) response).getServletResponse();
        String path = request.getURI().getPath();
        int status = servletResponse.getStatus();

        if (body instanceof ErrorResponse errorResponse) {
            String code = errorResponse.getCode();
            String message = errorResponse.getMessage();
            return ApiResponse.error(message, path, status, code);

        }

        if (status == 201) {
            return ApiResponse.created(body, path);
        }
        if (status == 204) {
            return ApiResponse.noContent(body, path);
        }

        return ApiResponse.ok(body, path);
    }
}
