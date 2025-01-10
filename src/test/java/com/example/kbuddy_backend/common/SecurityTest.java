package com.example.kbuddy_backend.common;

import com.example.kbuddy_backend.auth.config.JwtAccessDeniedHandler;
import com.example.kbuddy_backend.auth.config.JwtAuthenticationEntryPoint;
import com.example.kbuddy_backend.common.config.SecurityConfig;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, JwtAccessDeniedHandler.class})
public @interface SecurityTest {
}
